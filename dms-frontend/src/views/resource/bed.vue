<template>
  <div>
    <el-card shadow="never">
      <el-form :inline="true">
        <el-form-item label="楼栋">
          <el-select v-model="buildingId" placeholder="选择楼栋" style="width: 150px" @change="onBuildingChange">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-select v-model="floorId" placeholder="选择楼层" style="width: 120px" @change="onFloorChange">
            <el-option v-for="f in floors" :key="f.id" :label="f.floorName || f.floorNumber" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间">
          <el-select v-model="roomId" placeholder="选择房间" style="width: 120px" @change="reload">
            <el-option v-for="r in rooms" :key="r.id" :label="r.roomNumber" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :disabled="!roomId" @click="openCreate">新增床位</el-button>
          <el-button :loading="exporting" @click="onExport">导出</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="bedNumber" label="床位编号" width="120" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ labelOf(BED_TYPE, row.bedType) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagTypeOf(BED_STATUS, row.status) as any">{{ labelOf(BED_STATUS, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑床位' : '新增床位'" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="床位编号" prop="bedNumber"><el-input v-model="form.bedNumber" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.bedType" style="width: 100%">
            <el-option v-for="t in BED_TYPE" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="s in BED_STATUS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
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
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageBuildings } from '@/api/building'
import { listFloors } from '@/api/floor'
import { pageRooms } from '@/api/room'
import { listBeds, createBed, updateBed, deleteBed } from '@/api/bed'
import type { Building, Floor, Room, Bed } from '@/api/types'
import { BED_TYPE, BED_STATUS, labelOf, tagTypeOf } from '@/utils/dict'
import { exportLedger } from '@/api/export'

const buildings = ref<Building[]>([])
const floors = ref<Floor[]>([])
const rooms = ref<Room[]>([])
const list = ref<Bed[]>([])
const buildingId = ref<number>()
const floorId = ref<number>()
const roomId = ref<number>()
const loading = ref(false)
const exporting = ref(false)
const saving = ref(false)

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<Bed>>({})
const rules = { bedNumber: [{ required: true, message: '请输入床位编号', trigger: 'blur' }] }

async function loadBuildings() {
  const res = await pageBuildings({ page: 1, size: 100 })
  buildings.value = res.records
}

async function onBuildingChange() {
  floorId.value = undefined; roomId.value = undefined; rooms.value = []; list.value = []
  floors.value = buildingId.value ? await listFloors(buildingId.value) : []
}

async function onFloorChange() {
  roomId.value = undefined; list.value = []
  rooms.value = floorId.value ? (await pageRooms({ floorId: floorId.value, page: 1, size: 100 })).records : []
}

async function reload() {
  if (!roomId.value) return
  loading.value = true
  try {
    list.value = await listBeds(roomId.value)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { id: undefined, roomId: roomId.value, bedNumber: '', bedType: 3, status: 1 })
  dialogVisible.value = true
}

function openEdit(row: Bed) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    form.roomId = roomId.value
    if (form.id) {
      await updateBed(form.id, form)
    } else {
      await createBed(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: Bed) {
  await ElMessageBox.confirm(`确认删除床位「${row.bedNumber}」？`, '提示', { type: 'warning' })
  await deleteBed(row.id)
  ElMessage.success('删除成功')
  reload()
}

async function onExport() {
  exporting.value = true
  try {
    await exportLedger('beds', { roomId: roomId.value })
  } finally {
    exporting.value = false
  }
}

onMounted(loadBuildings)
</script>
