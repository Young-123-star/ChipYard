<template>
  <div>
    <el-card shadow="never">
      <el-form :inline="true" :model="query" @keyup.enter="reload">
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
          <el-button @click="reload">查询</el-button>
          <el-button type="primary" @click="openCreate">新增</el-button>
          <el-button :loading="exporting" @click="onExport">导出</el-button>
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
              <div v-else class="expand-loading">床位加载中...</div>
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
        <el-table-column label="水电账户" min-width="150">
          <template #default="{ row }">
            <span v-if="row.settlementMode">{{ row.settlementMode === 1 ? '户级' : '房间' }} · {{ row.utilityAccountCode }}</span>
            <el-tag v-else type="danger" size="small">未配置</el-tag>
          </template>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房间' : '新增房间'" width="620px">
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
        <el-divider content-position="left">水电结算</el-divider>
        <el-form-item label="结算方式">
          <el-select v-model="form.settlementMode" clearable placeholder="未配置" style="width: 100%">
            <el-option v-for="item in SETTLEMENT_MODE" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <template v-if="form.settlementMode">
          <el-form-item label="账户编码">
            <el-input v-model="form.utilityAccountCode" :placeholder="form.settlementMode === 2 ? '留空则使用房间号' : '同一户填写相同编码'" />
          </el-form-item>
          <el-form-item label="用电规则">
            <el-select v-model="form.electricityRule" style="width: 100%">
              <el-option v-for="item in ELECTRIC_RULE" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="用水规则">
            <el-select v-model="form.waterRule" style="width: 100%">
              <el-option v-for="item in WATER_RULE" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </template>
        <el-form-item label="配套设施">
          <div class="facility-editor">
            <div v-for="(row, index) in facilityRows" :key="index" class="facility-row">
              <el-select
                v-model="row.name"
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入设施"
                class="facility-name"
              >
                <el-option v-for="option in facilityOptions" :key="option.value" :label="option.label" :value="String(option.value)" />
              </el-select>
              <el-input-number v-model="row.count" :min="1" :max="99" controls-position="right" class="facility-count" />
              <el-button link type="danger" @click="removeFacilityRow(index)">删除</el-button>
            </div>
            <el-button type="primary" plain @click="addFacilityRow">添加设施</el-button>
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
import { ROOM_TYPE, ROOM_STATUS, GENDER_LIMIT, BED_TYPE, BED_STATUS, ROOM_FACILITY, labelOf, tagTypeOf, loadDictOptions, clearDictCache, type DictOption } from '@/utils/dict'
import { exportLedger } from '@/api/export'

const route = useRoute()
const buildings = ref<Building[]>([])
const floors = ref<Floor[]>([])
const formFloors = ref<Floor[]>([])
const list = ref<Room[]>([])
const total = ref(0)
const loading = ref(false)
const exporting = ref(false)
const saving = ref(false)
const bedsMap = reactive<Record<number, Bed[] | undefined>>({})
const summary = reactive<RoomSummary>({ total: 0, totalBeds: 0, occupiedBeds: 0, freeBeds: 0 })
const query = reactive({ buildingId: undefined as number | undefined, floorId: undefined as number | undefined, roomType: undefined as number | undefined, status: undefined as number | undefined, page: 1, size: 10 })

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<Room>>({})
const facilityOptions = ref<DictOption[]>(ROOM_FACILITY)
type FacilityRow = { name: string; count: number }
const facilityRows = ref<FacilityRow[]>([])
const SETTLEMENT_MODE = [{ value: 1, label: '户级账户' }, { value: 2, label: '房间账户' }]
const ELECTRIC_RULE = [
  { value: 0, label: '不计电费' }, { value: 1, label: '户级250度' }, { value: 2, label: '房间250度' },
  { value: 3, label: '夫妻实际费用平摊' }, { value: 4, label: '公司承担' }
]
const WATER_RULE = [
  { value: 0, label: '不计水费' }, { value: 1, label: '户级50吨' }, { value: 2, label: '房间17吨' },
  { value: 3, label: '夫妻实际费用平摊' }, { value: 4, label: '公司承担' }
]
const rules = {
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  roomType: [{ required: true, message: '请选择房型', trigger: 'change' }],
  bedCount: [{ required: true, message: '请输入床位数', trigger: 'blur' }]
}

const LEGACY_FACILITY_NAMES: Record<string, string> = {
  air_conditioner: '空调',
  water_heater: '热水器',
  wardrobe: '衣柜',
  desk: '书桌'
}

async function loadFacilityOptions(refresh = false) {
  if (refresh) clearDictCache('ROOM_FACILITY')
  facilityOptions.value = await loadDictOptions('ROOM_FACILITY', ROOM_FACILITY)
}

function normalizeFacilityName(name: string): string {
  return LEGACY_FACILITY_NAMES[name] || name
}

function loadFacilities(json?: string) {
  facilityRows.value = []
  if (!json) return
  try {
    const obj = JSON.parse(json) as Record<string, unknown>
    facilityRows.value = Object.entries(obj)
      .map(([key, value]) => ({ name: normalizeFacilityName(key), count: Number(value) }))
      .filter((item) => item.name && Number.isFinite(item.count) && item.count > 0)
  } catch {
    facilityRows.value = []
  }
}

function addFacilityRow() {
  facilityRows.value.push({ name: '', count: 1 })
}

function removeFacilityRow(index: number) {
  facilityRows.value.splice(index, 1)
}

function serializeFacilities(): string {
  const obj: Record<string, number> = {}
  facilityRows.value.forEach((row) => {
    const name = row.name.trim()
    const count = Number(row.count)
    if (!name || !Number.isFinite(count) || count <= 0) return
    obj[name] = (obj[name] || 0) + Math.floor(count)
  })
  return Object.keys(obj).length ? JSON.stringify(obj) : ''
}

function parseFacilities(json?: string): string[] {
  if (!json) return []
  try {
    const obj = JSON.parse(json) as Record<string, unknown>
    return Object.entries(obj)
      .filter(([, v]) => Number(v) > 0)
      .map(([k, v]) => {
        const name = normalizeFacilityName(k)
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
  await ElMessageBox.confirm(`确认将房间“${row.roomNumber}”${label}？`, '提示', { type: 'warning' })
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
  await loadFacilityOptions()
  Object.assign(form, {
    id: undefined, buildingId: undefined, floorId: undefined, roomNumber: '', roomType: 2, bedCount: 1,
    area: undefined, orientation: '', genderLimit: 0, status: 1, facilities: '',
    settlementMode: undefined, utilityAccountCode: '', electricityRule: 0, waterRule: 0
  })
  loadFacilities('')
  formFloors.value = []
  dialogVisible.value = true
}


async function openEdit(row: Room) {
  await loadFacilityOptions()
  Object.assign(form, row)
  loadFacilities(row.facilities)
  formFloors.value = await listFloors(row.buildingId)
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = { ...form, facilities: serializeFacilities() }
    if (form.id) {
      await updateRoom(form.id, payload)
    } else {
      await createRoom(payload)
    }
    await loadFacilityOptions(true)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

async function onDelete(row: Room) {
  await ElMessageBox.confirm(`确认删除房间“${row.roomNumber}”？`, '提示', { type: 'warning' })
  await deleteRoom(row.id)
  ElMessage.success('删除成功')
  reload()
}

async function onExport() {
  exporting.value = true
  try {
    await exportLedger('rooms', { ...query })
  } finally {
    exporting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadBuildings(), loadFacilityOptions()])
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
  margin-bottom: 12px;
  padding: 9px 16px;
  background: rgba(0, 113, 227, 0.06);
  border: 1px solid rgba(0, 113, 227, 0.12);
  border-radius: 10px;
  font-size: 13px;
  color: var(--dms-ink-2);
}
.summary-bar b { color: var(--dms-ink); font-weight: 700; margin: 0 2px; }
.summary-bar b.free { color: #1d8a3e; }
.fac-tag { margin-right: 4px; margin-bottom: 4px; }
.fac-none { color: var(--dms-ink-2); }
.facility-editor { width: 100%; display: flex; flex-direction: column; gap: 10px; }
.facility-row { display: grid; grid-template-columns: minmax(180px, 1fr) 130px 48px; align-items: center; gap: 10px; }
.facility-name,
.facility-count { width: 100%; }
.expand-box { padding: 6px 16px 14px 48px; }
.expand-info { display: flex; gap: 24px; font-size: 13px; color: var(--dms-ink-2); margin-bottom: 10px; }
.expand-info b { color: var(--dms-ink); font-weight: 600; margin-left: 4px; }
.bed-table { max-width: 480px; }
.expand-loading { font-size: 12.5px; color: var(--dms-ink-2); }
@media (max-width: 640px) {
  .facility-row { grid-template-columns: 1fr; }
}
</style>