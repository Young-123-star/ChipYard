<template>
  <div>
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="楼栋名称">
          <el-input v-model="query.buildingName" placeholder="名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="s in BUILDING_STATUS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="reload">查询</el-button>
          <el-button type="success" @click="openCreate">新增</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="buildingCode" label="编码" width="100" />
        <el-table-column prop="buildingName" label="名称" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="floorCount" label="楼层数" width="90" />
        <el-table-column label="电梯" width="80">
          <template #default="{ row }">{{ row.hasElevator === 1 ? '有' : '无' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagTypeOf(BUILDING_STATUS, row.status) as any">{{ labelOf(BUILDING_STATUS, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 12px; justify-content: flex-end"
        layout="total, prev, pager, next"
        :total="total"
        :current-page="query.page"
        :page-size="query.size"
        @current-change="onPageChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑楼栋' : '新增楼栋'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="编码" prop="buildingCode">
          <el-input v-model="form.buildingCode" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="名称" prop="buildingName"><el-input v-model="form.buildingName" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
        <el-form-item label="楼层数" prop="floorCount"><el-input-number v-model="form.floorCount" :min="1" /></el-form-item>
        <el-form-item label="电梯">
          <el-switch v-model="form.hasElevator" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option v-for="s in BUILDING_STATUS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageBuildings, createBuilding, updateBuilding, deleteBuilding } from '@/api/building'
import type { Building } from '@/api/types'
import { BUILDING_STATUS, labelOf, tagTypeOf } from '@/utils/dict'

const loading = ref(false)
const saving = ref(false)
const list = ref<Building[]>([])
const total = ref(0)
const query = reactive({ buildingName: '', status: undefined as number | undefined, page: 1, size: 10 })

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<Building>>({})
const rules = {
  buildingCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  buildingName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  floorCount: [{ required: true, message: '请输入楼层数', trigger: 'blur' }]
}

async function reload() {
  loading.value = true
  try {
    const res = await pageBuildings(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  query.page = p
  reload()
}

function openCreate() {
  Object.assign(form, { id: undefined, buildingCode: '', buildingName: '', address: '', floorCount: 1, hasElevator: 0, status: 1, remark: '' })
  dialogVisible.value = true
}

function openEdit(row: Building) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateBuilding(form.id, form)
    } else {
      await createBuilding(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: Building) {
  await ElMessageBox.confirm(`确认删除楼栋「${row.buildingName}」？`, '提示', { type: 'warning' })
  await deleteBuilding(row.id)
  ElMessage.success('删除成功')
  reload()
}

onMounted(reload)
</script>
