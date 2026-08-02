<template>
  <DataView title="居住人列表" :total="total">
    <template #filters>
      <el-input v-model="query.realName" placeholder="姓名" clearable style="width: 160px" @keyup.enter="search" />
      <el-input v-model="query.employeeNo" placeholder="工号" clearable style="width: 160px" @keyup.enter="search" />
      <el-select v-model="query.residentType" placeholder="类型：全部" clearable style="width: 120px">
        <el-option v-for="t in RESIDENT_TYPE" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
    </template>
    <template #filter-actions>
      <el-button type="primary" @click="search">查询</el-button>
    </template>
    <template #actions>
      <el-button :loading="exporting" @click="onExport">导出</el-button>
      <el-button type="primary" @click="openCreate">新增</el-button>
    </template>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="居住人" width="140">
        <template #default="{ row }">
          <div class="cell-main">{{ row.realName }}</div>
          <div class="cell-sub">{{ row.employeeNo }}</div>
        </template>
      </el-table-column>
      <el-table-column label="性别" width="80">
        <template #default="{ row }">{{ labelOf(GENDER, row.gender) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ labelOf(RESIDENT_TYPE, row.residentType) }}</template>
      </el-table-column>
      <el-table-column prop="deptName" label="部门" />
      <el-table-column prop="phone" label="手机" width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="tagTypeOf(RESIDENT_STATUS, row.status)" size="small" round>{{ labelOf(RESIDENT_STATUS, row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #pagination>
      <el-pagination layout="total, prev, pager, next" :total="total" :current-page="query.page" :page-size="query.size"
        @current-change="onPageChange" />
    </template>
  </DataView>

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑居住人' : '新增居住人'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender"><el-option v-for="g in GENDER" :key="g.value" :label="g.label" :value="g.value" /></el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.residentType"><el-option v-for="t in RESIDENT_TYPE" :key="t.value" :label="t.label" :value="t.value" /></el-select>
        </el-form-item>
        <el-form-item label="部门"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status"><el-option v-for="s in RESIDENT_STATUS" :key="s.value" :label="s.label" :value="s.value" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageResidents, createResident, updateResident, deleteResident } from '@/api/resident'
import type { Resident } from '@/api/types'
import { RESIDENT_TYPE, RESIDENT_STATUS, labelOf, tagTypeOf } from '@/utils/dict'
import { exportLedger } from '@/api/export'
import DataView from '@/components/layout/DataView.vue'

// 人的性别（区别于床位性别限制 GENDER_LIMIT，无「不限」项）：1=男、2=女
const GENDER = [
  { value: 1, label: '男', type: 'primary' },
  { value: 2, label: '女', type: 'danger' }
]

const loading = ref(false)
const exporting = ref(false)
const saving = ref(false)
const list = ref<Resident[]>([])
const total = ref(0)
const query = reactive({ realName: '', employeeNo: '', residentType: undefined as number | undefined, page: 1, size: 10 })
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<Resident>>({})
const rules = {
  employeeNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function reload() {
  loading.value = true
  try {
    const res = await pageResidents(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
// 查询：重置到第 1 页再加载（翻页仍走 reload）
function search() { query.page = 1; reload() }
function onPageChange(p: number) { query.page = p; reload() }
function openCreate() {
  // 表单默认值：性别男（1）、类型员工（1）、状态在职（1）
  Object.assign(form, { id: undefined, employeeNo: '', realName: '', gender: 1, residentType: 1, deptName: '', phone: '', status: 1 })
  dialogVisible.value = true
}
function openEdit(row: Resident) { Object.assign(form, row); dialogVisible.value = true }
async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.id) await updateResident(form.id, form)
    else await createResident(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}
async function onDelete(row: Resident) {
  try {
    await ElMessageBox.confirm(`确认删除居住人「${row.realName}」？`, '提示', { type: 'warning' })
  } catch {
    return // 用户取消
  }
  await deleteResident(row.id)
  ElMessage.success('删除成功')
  reload()
}
async function onExport() {
  exporting.value = true
  try {
    await exportLedger('residents', { ...query })
  } finally {
    exporting.value = false
  }
}

onMounted(reload)
</script>
