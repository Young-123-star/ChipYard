<template>
  <div class="import-page">
    <section class="intro">
      <div>
        <h2>数据初始化</h2>
        <p>按顺序导入宿舍资源、居住人、存量入住数据。先校验，全部通过后再执行导入。</p>
      </div>
    </section>

    <div class="steps">
      <div v-for="item in items" :key="item.type" class="step-box">
        <div class="step-head">
          <span class="step-no">{{ item.no }}</span>
          <div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
        </div>

        <div class="actions">
          <el-button @click="download(item.type, false)">下载模板</el-button>
          <el-button @click="download(item.type, true)">下载样例</el-button>
          <label class="file-btn">
            选择文件
            <input type="file" accept=".xlsx" @change="onFileChange(item.type, $event)" />
          </label>
          <span class="filename">{{ state[item.type].file?.name || '未选择文件' }}</span>
        </div>

        <div class="actions">
          <el-button type="primary" :loading="state[item.type].validating" @click="validate(item.type)">上传校验</el-button>
          <el-button type="success" :disabled="!state[item.type].result?.success" :loading="state[item.type].executing" @click="execute(item.type)">确认导入</el-button>
        </div>

        <div v-if="state[item.type].result" class="result">
          <el-alert
            :type="state[item.type].result?.success ? 'success' : 'error'"
            :closable="false"
            :title="summaryOf(state[item.type].result)"
            show-icon
          />
          <el-table v-if="state[item.type].result?.errors.length" :data="state[item.type].result?.errors" size="small" border class="error-table">
            <el-table-column prop="rowNumber" label="行号" width="80" />
            <el-table-column prop="field" label="字段" width="140" />
            <el-table-column prop="value" label="原始值" width="160" />
            <el-table-column prop="message" label="错误原因" />
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { downloadImportFile, executeImport, validateImport, type ImportResult, type ImportType } from '@/api/import'

interface StepState {
  file?: File
  result?: ImportResult
  validating: boolean
  executing: boolean
}

const items: Array<{ no: number; type: ImportType; title: string; desc: string }> = [
  { no: 1, type: 'resource', title: '宿舍资源', desc: '楼栋、楼层、房间、床位' },
  { no: 2, type: 'resident', title: '居住人', desc: '员工、外协和其他居住人档案' },
  { no: 3, type: 'checkin-record', title: '存量入住', desc: '按工号分配床位并生成入住档案' }
]

const state = reactive<Record<ImportType, StepState>>({
  resource: { validating: false, executing: false },
  resident: { validating: false, executing: false },
  'checkin-record': { validating: false, executing: false }
})

function onFileChange(type: ImportType, event: Event) {
  const input = event.target as HTMLInputElement
  state[type].file = input.files?.[0]
  state[type].result = undefined
}

async function download(type: ImportType, sample: boolean) {
  const blob = await downloadImportFile(type, sample)
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${type}-${sample ? 'sample' : 'template'}.xlsx`
  a.click()
  URL.revokeObjectURL(url)
}

async function validate(type: ImportType) {
  const file = state[type].file
  if (!file) return ElMessage.warning('请先选择 xlsx 文件')
  state[type].validating = true
  try {
    state[type].result = await validateImport(type, file)
  } finally {
    state[type].validating = false
  }
}

async function execute(type: ImportType) {
  const file = state[type].file
  if (!file) return ElMessage.warning('请先选择 xlsx 文件')
  state[type].executing = true
  try {
    state[type].result = await executeImport(type, file)
    if (state[type].result?.success) ElMessage.success('导入完成')
  } finally {
    state[type].executing = false
  }
}

function summaryOf(result?: ImportResult) {
  if (!result) return ''
  return result.success
    ? `校验通过，共 ${result.totalRows} 行`
    : `校验失败，共 ${result.totalRows} 行，${result.errors.length} 个错误`
}
</script>

<style scoped>
.import-page { display: flex; flex-direction: column; gap: 16px; }
.intro {
  display: flex; justify-content: space-between; align-items: center;
  padding: 18px 20px; border: 1px solid var(--dms-hairline); border-radius: 8px; background: var(--dms-surface);
}
.intro h2 { margin: 0 0 6px; font-size: 20px; }
.intro p { margin: 0; color: var(--dms-ink-2); font-size: 13px; }
.steps { display: grid; gap: 14px; }
.step-box { border: 1px solid var(--dms-hairline); border-radius: 8px; background: var(--dms-surface); padding: 18px 20px; }
.step-head { display: flex; gap: 12px; align-items: flex-start; margin-bottom: 14px; }
.step-no { width: 28px; height: 28px; border-radius: 50%; background: var(--dms-accent); color: #fff; display: inline-flex; align-items: center; justify-content: center; font-weight: 700; }
.step-head h3 { margin: 0 0 4px; font-size: 16px; }
.step-head p { margin: 0; color: var(--dms-ink-2); font-size: 13px; }
.actions { display: flex; align-items: center; gap: 10px; margin-top: 10px; flex-wrap: wrap; }
.file-btn { position: relative; overflow: hidden; display: inline-flex; align-items: center; height: 32px; padding: 0 14px; border: 1px solid var(--el-border-color); border-radius: 4px; cursor: pointer; font-size: 14px; background: #fff; }
.file-btn input { position: absolute; inset: 0; opacity: 0; cursor: pointer; }
.filename { color: var(--dms-ink-2); font-size: 13px; }
.result { margin-top: 12px; }
.error-table { margin-top: 10px; }
</style>
