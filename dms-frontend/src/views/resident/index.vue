<template>
  <el-card shadow="never">
    <el-form :inline="true" :model="query">
      <el-form-item label="姓名"><el-input v-model="query.realName" placeholder="姓名" clearable /></el-form-item>
      <el-form-item label="工号"><el-input v-model="query.employeeNo" placeholder="工号" clearable /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.residentType" placeholder="全部" clearable style="width: 120px">
          <el-option v-for="t in RESIDENT_TYPE" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="reload">查询</el-button>
        <el-button type="success" @click="openCreate">新增</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column prop="employeeNo" label="工号" width="120" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column label="性别" width="80">
        <template #default="{ row }">{{ labelOf(GENDER_LIMIT, row.gender) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ labelOf(RESIDENT_TYPE, row.residentType) }}</template>
      </el-table-column>
      <el-table-column prop="deptName" label="部门" />
      <el-table-column prop="phone" label="手机" width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="tagTypeOf(RESIDENT_STATUS, row.status) as any" size="small" round>{{ labelOf(RESIDENT_STATUS, row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > query.size" style="margin-top: 12px; justify-content: flex-end"
      layout="total, prev, pager, next" :total="total" :current-page="query.page" :page-size="query.size"
      @current-change="onPageChange" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑居住人' : '新增居住人'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender"><el-option v-for="g in GENDER_LIMIT" :key="g.value" :label="g.label" :value="g.value" /></el-select>
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
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageResidents, createResident, updateResident, deleteResident } from '@/api/resident'
import type { Resident } from '@/api/types'
import { RESIDENT_TYPE, RESIDENT_STATUS, GENDER_LIMIT, labelOf, tagTypeOf } from '@/utils/dict'

const loading = ref(false)
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
function onPageChange(p: number) { query.page = p; reload() }
function openCreate() {
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
  await ElMessageBox.confirm(`确认删除居住人「${row.realName}」？`, '提示', { type: 'warning' })
  await deleteResident(row.id)
  ElMessage.success('删除成功')
  reload()
}
onMounted(reload)
</script>
