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

      <div class="summary-bar">
        当前筛选：共 <b>{{ summary.total }}</b> 间 · 床位 <b>{{ summary.totalBeds }}</b> · 已住 <b>{{ summary.occupiedBeds }}</b> · 空闲 <b class="free">{{ summary.freeBeds }}</b>
      </div>

      <el-table :data="list" v-loading="loading" border @expand-change="onExpand">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-box">
              <div class="expand-info">
                <span>面积 <b>{{ row.area ?? '-' }}㎡</b></span>
                <span>朝向 <b>{{ row.orientation || '-' }}</b></span>
                <span v-if="row.remark">备注 <b>{{ row.remark }}</b></span>
              </div>
              <el-table v-if="bedsMap[row.id]" :data="bedsMap[row.id]" size="small" class="bed-table">
                <el-table-column prop="bedNumber" label="床位编号" width="120" />
                <el-table-column label="类型" width="100">
                  <template #default="{ row: bed }">{{ labelOf(BED_TYPE, bed.bedType) }}</template>
                </el-table-column>
                <el-table-column label="状态" width="120">
                  <template #default="{ row: bed }">
                    <el-tag :type="tagTypeOf(BED_STATUS, bed.status) as any" size="small">{{ labelOf(BED_STATUS, bed.status) }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <div v-else class="expand-loading">床位加载中…</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="roomNumber" label="房间号" width="100" />
        <el-table-column label="房型" width="90">
          <template #default="{ row }">{{ labelOf(ROOM_TYPE, row.roomType) }}</template>
        </el-table-column>
        <el-table-column label="床位" width="100">
          <template #default="{ row }">{{ row.occupiedBeds }}/{{ row.bedCount }}</template>
        </el-table-column>
        <el-table-column label="配套设施" min-width="180">
          <template #default="{ row }">
            <template v-if="parseFacilities(row.facilities).length">
              <el-tag v-for="f in parseFacilities(row.facilities)" :key="f" size="small" class="fac-tag" type="info" effect="plain">{{ f }}</el-tag>
            </template>
            <span v-else class="fac-none">-</span>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ labelOf(GENDER_LIMIT, row.genderLimit) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="tagTypeOf(ROOM_STATUS, row.status) as any">{{ labelOf(ROOM_STATUS, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="225">
          <template #default="{ row }">
            <el-button v-if="row.status !== 3" link type="warning" @click="markRepair(row, true)">标记维修</el-button>
            <el-button v-else link type="success" @click="markRepair(row, false)">恢复空闲</el-button>
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
        <el-form-item label="配套设施">
          <div class="facility-editor">
            <div v-for="item in FACILITY_OPTIONS" :key="item.key" class="facility-row">
              <el-checkbox v-model="facilityValues[item.key].enabled">{{ item.label }}</el-checkbox>
              <el-input-number v-model="facilityValues[item.key].count" :min="1" :max="20" size="small" :disabled="!facilityValues[item.key].enabled" />
            </div>
          </div>
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageBuildings } from '@/api/building'
import { listFloors } from '@/api/floor'
import { pageRooms, roomSummary, createRoom, updateRoom, deleteRoom } from '@/api/room'
import { listBeds } from '@/api/bed'
import type { Building, Floor, Room, Bed, RoomSummary } from '@/api/types'
import { ROOM_TYPE, ROOM_STATUS, GENDER_LIMIT, BED_TYPE, BED_STATUS, labelOf, tagTypeOf } from '@/utils/dict'

const route = useRoute()
const buildings = ref<Building[]>([])
const floors = ref<Floor[]>([])
const formFloors = ref<Floor[]>([])
const list = ref<Room[]>([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const bedsMap = reactive<Record<number, Bed[] | undefined>>({})
const summary = reactive<RoomSummary>({ total: 0, totalBeds: 0, occupiedBeds: 0, freeBeds: 0 })
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

const FACILITY_OPTIONS = [
  { key: 'air_conditioner', label: '空调' },
  { key: 'water_heater', label: '热水器' },
  { key: 'wardrobe', label: '衣柜' },
  { key: 'desk', label: '书桌' }
] as const

type FacilityKey = typeof FACILITY_OPTIONS[number]['key']

type FacilityValue = { enabled: boolean; count: number }

const FACILITY_NAMES: Record<string, string> = Object.fromEntries(FACILITY_OPTIONS.map((item) => [item.key, item.label]))
const facilityValues = reactive<Record<FacilityKey, FacilityValue>>(
  Object.fromEntries(FACILITY_OPTIONS.map((item) => [item.key, { enabled: false, count: 1 }])) as Record<FacilityKey, FacilityValue>
)
const extraFacilities = ref<Record<string, number>>({})

function resetFacilityValues() {
  FACILITY_OPTIONS.forEach((item) => {
    facilityValues[item.key].enabled = false
    facilityValues[item.key].count = 1
  })
  extraFacilities.value = {}
}

function loadFacilities(json?: string) {
  resetFacilityValues()
  if (!json) return
  try {
    const obj = JSON.parse(json) as Record<string, unknown>
    Object.entries(obj).forEach(([k, v]) => {
      const count = Number(v)
      if (!Number.isFinite(count) || count <= 0) return
      if (Object.prototype.hasOwnProperty.call(facilityValues, k)) {
        const key = k as FacilityKey
        facilityValues[key].enabled = true
        facilityValues[key].count = count
      } else {
        extraFacilities.value[k] = count
      }
    })
  } catch {
    extraFacilities.value = {}
  }
}

function serializeFacilities(): string {
  const obj: Record<string, number> = { ...extraFacilities.value }
  FACILITY_OPTIONS.forEach((item) => {
    if (facilityValues[item.key].enabled) obj[item.key] = facilityValues[item.key].count || 1
    else delete obj[item.key]
  })
  return Object.keys(obj).length ? JSON.stringify(obj) : ''
}

function parseFacilities(json?: string): string[] {
  if (!json) return []
  try {
    const obj = JSON.parse(json)
    return Object.entries(obj)
      .filter(([, v]) => Number(v) > 0)
      .map(([k, v]) => {
        const name = FACILITY_NAMES[k] || k
        return Number(v) > 1 ? `${name}x${v}` : name
      })
  } catch {
    return []
  }
}

async function onExpand(row: Room, expanded: Room[]) {
  if (expanded.includes(row) && !bedsMap[row.id]) {
    bedsMap[row.id] = await listBeds(row.id)
  }
}

async function markRepair(row: Room, toRepair: boolean) {
  const target = toRepair ? 3 : 1
  const label = toRepair ? '标记维修' : '恢复空闲'
  await ElMessageBox.confirm(`确认将房间「${row.roomNumber}」${label}？`, '提示', { type: 'warning' })
  await updateRoom(row.id, { ...row, status: target })
  ElMessage.success(`已${label}`)
  reload()
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
    const [res, sum] = await Promise.all([pageRooms(query), roomSummary(query)])
    list.value = res.records
    total.value = res.total
    Object.assign(summary, sum)
  } finally {
    loading.value = false
  }
}

async function openCreate() {
  Object.assign(form, { id: undefined, buildingId: undefined, floorId: undefined, roomNumber: '', roomType: 2, bedCount: 1, area: undefined, orientation: '', genderLimit: 0, status: 1, facilities: '' })
  loadFacilities('')
  formFloors.value = []
  dialogVisible.value = true
}

async function openEdit(row: Room) {
  Object.assign(form, row)
  loadFacilities(row.facilities)
  formFloors.value = await listFloors(row.buildingId)
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    form.facilities = serializeFacilities()
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

onMounted(async () => {
  await loadBuildings()
  // 接收楼层管理页跳转参数：/rooms?buildingId=&floorId=
  const qb = Number(route.query.buildingId)
  const qf = Number(route.query.floorId)
  if (qb) {
    query.buildingId = qb
    floors.value = await listFloors(qb)
    if (qf) query.floorId = qf
  }
  reload()
})
</script>

<style scoped>
.summary-bar {
  margin-bottom: 12px; padding: 9px 16px;
  background: rgba(0, 113, 227, 0.06);
  border: 1px solid rgba(0, 113, 227, 0.12);
  border-radius: 10px;
  font-size: 13px; color: var(--dms-ink-2);
}
.summary-bar b { color: var(--dms-ink); font-weight: 700; margin: 0 2px; }
.summary-bar b.free { color: #1d8a3e; }
.fac-tag { margin-right: 4px; }
.fac-none { color: var(--dms-ink-2); }
.facility-editor { width: 100%; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px 16px; }
.facility-row { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.expand-box { padding: 6px 16px 14px 48px; }
.expand-info { display: flex; gap: 24px; font-size: 13px; color: var(--dms-ink-2); margin-bottom: 10px; }
.expand-info b { color: var(--dms-ink); font-weight: 600; margin-left: 4px; }
.bed-table { max-width: 480px; }
.expand-loading { font-size: 12.5px; color: var(--dms-ink-2); }
</style>
