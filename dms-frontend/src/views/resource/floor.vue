<template>
  <div>
    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="选择楼栋">
          <el-select v-model="buildingId" placeholder="请选择楼栋" style="width: 220px" @change="reload">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!buildingId" @click="openCreate">新增楼层</el-button>
          <el-button :loading="exporting" @click="onExport">导出</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="floorNumber" label="楼层号" width="100" />
        <el-table-column prop="floorName" label="名称" />
        <el-table-column prop="roomCount" label="房间数" width="100" />
        <el-table-column label="入住情况" min-width="200">
          <template #default="{ row }">
            <div class="occ">
              <el-progress
                :percentage="row.bedCount ? Math.round((row.occupiedBeds / row.bedCount) * 100) : 0"
                :stroke-width="8"
                :show-text="false"
                style="flex: 1"
              />
              <span class="occ-text">{{ row.occupiedBeds || 0 }}/{{ row.bedCount || 0 }} 床</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="goRooms(row)">查看房间</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑楼层' : '新增楼层'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="楼层号" prop="floorNumber"><el-input-number v-model="form.floorNumber" :min="1" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.floorName" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageBuildings } from '@/api/building'
import { listFloors, createFloor, updateFloor, deleteFloor } from '@/api/floor'
import type { Building, Floor } from '@/api/types'
import { exportLedger } from '@/api/export'

const router = useRouter()

function goRooms(row: Floor) {
  router.push({ path: '/rooms', query: { buildingId: String(row.buildingId), floorId: String(row.id) } })
}

const buildings = ref<Building[]>([])
const buildingId = ref<number>()
const list = ref<Floor[]>([])
const loading = ref(false)
const exporting = ref(false)
const saving = ref(false)

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<Floor>>({})
const rules = { floorNumber: [{ required: true, message: '请输入楼层号', trigger: 'blur' }] }

async function loadBuildings() {
  const res = await pageBuildings({ page: 1, size: 100 })
  buildings.value = res.records
  if (!buildingId.value && res.records.length) {
    buildingId.value = res.records[0].id
    await reload()
  }
}

async function reload() {
  if (!buildingId.value) return
  loading.value = true
  try {
    list.value = await listFloors(buildingId.value)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: undefined, buildingId: buildingId.value, floorNumber: 1, floorName: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row: Floor) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    form.buildingId = buildingId.value
    if (form.id) {
      await updateFloor(form.id, form)
    } else {
      await createFloor(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: Floor) {
  await ElMessageBox.confirm(`确认删除楼层「${row.floorName || row.floorNumber}」？`, '提示', { type: 'warning' })
  await deleteFloor(row.id)
  ElMessage.success('删除成功')
  reload()
}

async function onExport() {
  exporting.value = true
  try {
    await exportLedger('floors', { buildingId: buildingId.value })
  } finally {
    exporting.value = false
  }
}

onMounted(loadBuildings)
</script>

<style scoped>
.occ { display: flex; align-items: center; gap: 10px; padding-right: 8px; }
.occ-text { font-size: 12px; color: var(--dms-ink-2); white-space: nowrap; }
</style>
