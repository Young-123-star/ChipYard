<template>
  <el-card shadow="never">
    <div style="margin-bottom: 12px">
      <el-button type="primary" @click="openCreate">新增收费标准</el-button>
          <el-button :loading="exporting" @click="onExport">导出</el-button>
    </div>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="房型" width="160">
        <template #default="{ row }">{{ labelOf(ROOM_TYPE, row.roomType) }}</template>
      </el-table-column>
      <el-table-column label="月单价(元)" width="160">
        <template #default="{ row }">{{ row.monthlyPrice }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="visible" :title="form.id ? '编辑收费标准' : '新增收费标准'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="房型" prop="roomType">
          <el-select v-model="form.roomType" placeholder="选择房型" style="width: 100%">
            <el-option v-for="t in ROOM_TYPE" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="月单价" prop="monthlyPrice">
          <el-input-number v-model="form.monthlyPrice" :min="0" :precision="2" :step="50" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { listStandards, createStandard, updateStandard, deleteStandard } from '@/api/fee'
import type { FeeStandard } from '@/api/types'
import { ROOM_TYPE, labelOf } from '@/utils/dict'
import { exportLedger } from '@/api/export'

const loading = ref(false)
const exporting = ref(false)
const saving = ref(false)
const list = ref<FeeStandard[]>([])

const visible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<{ id?: number; roomType?: number; monthlyPrice?: number; remark?: string }>({})
const rules = {
  roomType: [{ required: true, message: '请选择房型', trigger: 'change' }],
  monthlyPrice: [{ required: true, message: '请输入月单价', trigger: 'blur' }]
}

async function reload() {
  loading.value = true
  try {
    list.value = await listStandards()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: undefined, roomType: undefined, monthlyPrice: undefined, remark: '' })
  visible.value = true
}
function openEdit(row: FeeStandard) {
  Object.assign(form, { id: row.id, roomType: row.roomType, monthlyPrice: row.monthlyPrice, remark: row.remark })
  visible.value = true
}
async function onSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = { roomType: form.roomType!, monthlyPrice: form.monthlyPrice!, remark: form.remark }
    if (form.id) await updateStandard(form.id, payload)
    else await createStandard(payload)
    ElMessage.success('已保存')
    visible.value = false
    reload()
  } finally {
    saving.value = false
  }
}
async function onDelete(row: FeeStandard) {
  await ElMessageBox.confirm(`确认删除「${labelOf(ROOM_TYPE, row.roomType)}」收费标准？`, '提示', { type: 'warning' })
  await deleteStandard(row.id)
  ElMessage.success('已删除')
  reload()
}

async function onExport() {
  exporting.value = true
  try {
    await exportLedger('fee-standards', undefined)
  } finally {
    exporting.value = false
  }
}

onMounted(reload)
</script>
