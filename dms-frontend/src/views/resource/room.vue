<template>
  <DataView title="房间列表" :total="total" collapsible>
    <template #filters>
      <el-select v-model="query.buildingId" placeholder="楼栋" clearable style="width: 160px" @change="onBuildingChange">
        <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
      </el-select>
      <el-select v-model="query.floorId" placeholder="楼层" clearable style="width: 120px">
        <el-option v-for="f in floors" :key="f.id" :label="f.floorName || f.floorNumber" :value="f.id" />
      </el-select>
      <el-select v-model="query.roomType" placeholder="房型" clearable style="width: 120px">
        <el-option v-for="t in ROOM_TYPE" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option v-for="s in ROOM_STATUS" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </template>
    <template #filter-actions>
      <el-button type="primary" @click="search">查询</el-button>
    </template>
    <template #actions>
      <el-button :loading="exporting" @click="onExport">导出</el-button>
      <el-button type="primary" @click="openCreate">新增</el-button>
    </template>

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
                    <el-tag :type="tagTypeOf(BED_STATUS, bed.status)" size="small">{{ labelOf(BED_STATUS, bed.status) }}</el-tag>
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
            <span v-if="row.settlementMode">{{ labelOf(SETTLEMENT_MODE, row.settlementMode) }} · {{ row.utilityAccountCode }}</span>
            <el-tag v-else type="danger" size="small">未配置</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配套设施" min-width="180">
          <template #default="{ row }">
            <template v-if="row.facilitiesList.length">
              <el-tag v-for="f in row.facilitiesList" :key="f" size="small" class="fac-tag" type="info" effect="plain">{{ f }}</el-tag>
            </template>
            <span v-else class="fac-none">-</span>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="70">
          <template #default="{ row }">{{ labelOf(GENDER_LIMIT, row.genderLimit) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="tagTypeOf(ROOM_STATUS, row.status)">{{ labelOf(ROOM_STATUS, row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status !== 3" link type="warning" @click="markRepair(row, true)">标记维修</el-button>
            <el-button v-else link type="success" @click="markRepair(row, false)">恢复空闲</el-button>
            <el-dropdown trigger="click">
              <el-button link type="primary">更多<el-icon><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="onDelete(row)">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

    <template #pagination>
      <el-pagination
        layout="total, prev, pager, next"
        :total="total" :current-page="query.page" :page-size="query.size"
        @current-change="(p: number) => { query.page = p; reload() }"
      />
    </template>
  </DataView>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑房间' : '新增房间'" width="720px">
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
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { pageRooms, roomSummary, createRoom, updateRoom, deleteRoom } from '@/api/room'
import { listBeds } from '@/api/bed'
import type { Room, Bed, RoomSummary } from '@/api/types'
import { ROOM_TYPE, ROOM_STATUS, GENDER_LIMIT, BED_TYPE, BED_STATUS, ROOM_FACILITY, SETTLEMENT_MODE, ELECTRIC_RULE, WATER_RULE, labelOf, tagTypeOf, loadDictOptions, clearDictCache, type DictOption } from '@/utils/dict'
import { parseFacilities, parseFacilityRows, serializeFacilities, type FacilityRow } from '@/utils/facility'
import { useRoomLocationOptions } from '@/composables/useRoomLocationOptions'
import { exportLedger } from '@/api/export'
import DataView from '@/components/layout/DataView.vue'

type RoomRow = Room & { facilitiesList: string[] }

const route = useRoute()
const { buildings, floors, loadBuildings, loadFloors } = useRoomLocationOptions()
const { floors: formFloors, loadFloors: loadFormFloors } = useRoomLocationOptions()
const list = ref<RoomRow[]>([])
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
const facilityRows = ref<FacilityRow[]>([])
const rules = {
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }],
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  roomType: [{ required: true, message: '请选择房型', trigger: 'change' }],
  bedCount: [{ required: true, message: '请输入床位数', trigger: 'blur' }]
}

async function loadFacilityOptions(refresh = false) {
  if (refresh) clearDictCache('ROOM_FACILITY')
  facilityOptions.value = await loadDictOptions('ROOM_FACILITY', ROOM_FACILITY)
}

function loadFacilities(json?: string) {
  facilityRows.value = parseFacilityRows(json)
}

function addFacilityRow() {
  facilityRows.value.push({ name: '', count: 1 })
}

function removeFacilityRow(index: number) {
  facilityRows.value.splice(index, 1)
}

async function onExpand(row: Room, expanded: Room[]) {
  if (expanded.includes(row) && !bedsMap[row.id]) {
    bedsMap[row.id] = await listBeds(row.id)
  }
}

async function markRepair(row: Room, toRepair: boolean) {
  const target = toRepair ? 3 : 1
  const label = toRepair ? '标记维修' : '恢复空闲'
  try {
    await ElMessageBox.confirm(`确认将房间“${row.roomNumber}”${label}？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await updateRoom(row.id, { ...row, status: target })
  ElMessage.success(`已${label}`)
  reload()
}

async function onBuildingChange() {
  query.floorId = undefined
  await loadFloors(query.buildingId)
}

async function onFormBuildingChange() {
  form.floorId = undefined
  await loadFormFloors(form.buildingId)
}

async function reload() {
  loading.value = true
  try {
    // 床位缓存随列表刷新一并失效，避免展示过期数据
    Object.keys(bedsMap).forEach((key) => { delete bedsMap[Number(key)] })
    const [res, sum] = await Promise.all([pageRooms(query), roomSummary(query)])
    list.value = res.records.map((room) => ({ ...room, facilitiesList: parseFacilities(room.facilities) }))
    total.value = res.total
    Object.assign(summary, sum)
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  reload()
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
  await loadFormFloors(row.buildingId)
  dialogVisible.value = true
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = { ...form, facilities: serializeFacilities(facilityRows.value) }
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
  try {
    await ElMessageBox.confirm(`确认删除房间“${row.roomNumber}”？`, '提示', { type: 'warning' })
  } catch {
    return
  }
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
    await loadFloors(qb)
    if (qf) query.floorId = qf
  }
  reload()
})
</script>

<style scoped>
.summary-bar {
  margin: 4px 0 12px;
  padding: 9px 16px;
  background: var(--dms-accent-soft);
  border: 1px solid var(--dms-hairline);
  border-radius: 10px;
  font-size: 13px;
  color: var(--dms-ink-2);
}
.summary-bar b { color: var(--dms-ink); font-weight: 700; margin: 0 2px; }
.summary-bar b.free { color: var(--dms-ok); }
.fac-tag { margin-right: 4px; margin-bottom: 4px; }
.fac-none { color: var(--dms-ink-2); }
.el-dropdown { margin-left: 8px; vertical-align: middle; }
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
