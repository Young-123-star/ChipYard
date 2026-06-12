<template>
  <div>
    <el-card shadow="never">
      <el-form :inline="true" :model="query">
        <el-form-item label="楼栋">
          <el-select v-model="query.buildingId" placeholder="全部" clearable style="width: 160px" @change="onBuildingChange">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-select v-model="query.floorId" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="f in floors" :key="f.id" :label="f.floorName || f.floorNumber" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="query.roomType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="t in ROOM_TYPE" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="s in ROOM_STATUS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="reload">查询</el-button>
          <el-button type="success" @click="openCreate">新增</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column label="房型" width="100">
          <template #default="{ row }">{{ labelOf(ROOM_TYPE, row.roomType) }}</template>
        </el-table-column>
        <el-table-column prop="area" label="面积㎡" width="90" />
        <el-table-column prop="orientation" label="朝向" width="80" />
        <el-table-column label="床位" width="100">
          <template #default="{ row }">{{ row.occupiedBeds }}/{{ row.bedCount }}</template>
        </el-table-column>
        <el-table-column label="性别限制" width="90">
          <template #default="{ row }">{{ labelOf(GENDER_LIMIT, row.genderLimit) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="tagTypeOf(ROOM_STATUS, row.status) as any">{{ labelOf(ROOM_STATUS, row.status) }}</el-tag>
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
        :total="total" :current-page="query.page" :page-size="query.size"
        @current-change="(p: number) => { query.page = p; reload() }"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房间' : '新增房间'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="form.buildingId" style="width: 100%" @change="onFormBuildingChange">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floorId">
          <el-select v-model="form.floorId" style="width: 100%">
            <el-option v-for="f in formFloors" :key="f.id" :label="f.floorName || f.floorNumber" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号" prop="roomNumber"><el-input v-model="form.roomNumber" /></el-form-item>
        <el-form-item label="房型" prop="roomType">
          <el-select v-model="form.roomType" style="width: 100%">
            <el-option v-for="t in ROOM_TYPE" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位数" prop="bedCount"><el-input-number v-model="form.bedCount" :min="1" /></el-form-item>
        <el-form-item label="面积"><el-input-number v-model="form.area" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="朝向"><el-input v-model="form.orientation" /></el-form-item>
        <el-form-item label="性别限制">
          <el-select v-model="form.genderLimit" style="width: 100%">
            <el-option v-for="g in GENDER_LIMIT" :key="g.value" :label="g.label" :value="g.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="s in ROOM_STATUS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="配套设施"><el-input v-model="form.facilities" type="textarea" placeholder='JSON，如 {"air_conditioner":1}' /></el-form-item>
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
import { pageRooms, createRoom, updateRoom, deleteRoom } from '@/api/room'
import type { Building, Floor, Room } from '@/api/types'
import { ROOM_TYPE, ROOM_STATUS, GENDER_LIMIT, labelOf, tagTypeOf } from '@/utils/dict'

const buildings = ref<Building[]>([])
const floors = ref<Floor[]>([])
const formFloors = ref<Floor[]>([])
const list = ref<Room[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const query = reactive({ buildingId: undefined as number | undefined, floorId: undefined as number | undefined, roomType: undefined as number | undefined, status: undefined as number | undefined, page: 1, size: 10 })

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<Room>>({})
const rules = {
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  roomType: [{ required: true, message: '请选择房型', trigger: 'change' }],
  bedCount: [{ required: true, message: '请输入床位数', trigger: 'blur' }]
}

async function loadBuildings() {
  const res = await pageBuildings({ page: 1, size: 100 })
  buildings.value = res.records
}

async function onBuildingChange() {
  query.floorId = undefined
  floors.value = query.buildingId ? await listFloors(query.buildingId) : []
}

async function onFormBuildingChange() {
  form.floorId = undefined
  formFloors.value = form.buildingId ? await listFloors(form.buildingId) : []
}

async function reload() {
  loading.value = true
  try {
    const res = await pageRooms(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function openCreate() {
  Object.assign(form, { id: undefined, buildingId: undefined, floorId: undefined, roomNumber: '', roomType: 2, bedCount: 1, area: undefined, orientation: '', genderLimit: 0, status: 1, facilities: '' })
  formFloors.value = []
  dialogVisible.value = true
}

async function openEdit(row: Room) {
  Object.assign(form, row)
  formFloors.value = await listFloors(row.buildingId)
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateRoom(form.id, form)
    } else {
      await createRoom(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: Room) {
  await ElMessageBox.confirm(`确认删除房间「${row.roomNumber}」？`, '提示', { type: 'warning' })
  await deleteRoom(row.id)
  ElMessage.success('删除成功')
  reload()
}

onMounted(() => { loadBuildings(); reload() })
</script>
