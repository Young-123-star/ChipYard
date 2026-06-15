<template>
  <el-card shadow="never">
    <el-form :inline="true" :model="query">
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px" @change="reload">
          <el-option v-for="s in INTAKE_STATUS" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源">
        <el-select v-model="query.source" placeholder="全部" clearable style="width: 120px" @change="reload">
          <el-option v-for="s in INTAKE_SOURCE" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="reload">查询</el-button>
        <el-button type="success" @click="openCreate">手工新建</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column prop="bizNo" label="业务号" width="160" />
      <el-table-column label="居住人" width="160">
        <template #default="{ row }">{{ row.residentName }}（{{ row.employeeNo }}）</template>
      </el-table-column>
      <el-table-column label="来源" width="90">
        <template #default="{ row }">{{ labelOf(INTAKE_SOURCE, row.source) }}</template>
      </el-table-column>
      <el-table-column label="性别要求" width="90">
        <template #default="{ row }">{{ labelOf(GENDER_LIMIT, row.genderLimitReq) }}</template>
      </el-table-column>
      <el-table-column prop="expectCheckinDate" label="预计入住" width="120" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="tagTypeOf(INTAKE_STATUS, row.status) as any" size="small" round>{{ labelOf(INTAKE_STATUS, row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <template v-if="row.status === 1">
            <el-button link type="primary" @click="openAssign(row)">分配入住</el-button>
            <el-button link type="danger" @click="onCancel(row)">取消</el-button>
          </template>
          <span v-else style="color: var(--dms-ink-2); font-size: 12px">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > query.size" style="margin-top: 12px; justify-content: flex-end"
      layout="total, prev, pager, next" :total="total" :current-page="query.page" :page-size="query.size"
      @current-change="onPageChange" />

    <!-- 手工新建 -->
    <el-dialog v-model="createVisible" title="手工新建入住意向单" width="460px">
      <el-form ref="createRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="居住人" prop="residentId">
          <el-select v-model="createForm.residentId" filterable placeholder="选择居住人" style="width: 100%">
            <el-option v-for="r in residents" :key="r.id" :label="`${r.realName}（${r.employeeNo}）`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计入住"><el-date-picker v-model="createForm.expectCheckinDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="性别要求">
          <el-select v-model="createForm.genderLimitReq" style="width: 100%"><el-option v-for="g in GENDER_LIMIT" :key="g.value" :label="g.label" :value="g.value" /></el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="createForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配入住 -->
    <el-dialog v-model="assignVisible" title="分配入住" width="500px">
      <el-descriptions :column="1" border size="small" style="margin-bottom: 14px">
        <el-descriptions-item label="居住人">{{ current?.residentName }}（{{ current?.employeeNo }}）</el-descriptions-item>
        <el-descriptions-item label="性别要求">{{ labelOf(GENDER_LIMIT, current?.genderLimitReq) }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px">
        <el-form-item label="楼栋">
          <el-select v-model="sel.buildingId" placeholder="选择楼栋" style="width: 100%" @change="onBuildingChange">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间">
          <el-select v-model="sel.roomId" placeholder="选择房间" style="width: 100%" @change="onRoomChange" :disabled="!sel.buildingId">
            <el-option v-for="r in rooms" :key="r.id" :label="`${r.roomNumber}（${labelOf(ROOM_TYPE, r.roomType)}，限${labelOf(GENDER_LIMIT, r.genderLimit)}）`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位">
          <el-select v-model="sel.bedId" placeholder="选择空闲床位" style="width: 100%" :disabled="!sel.roomId">
            <el-option v-for="bed in freeBeds" :key="bed.id" :label="`${bed.bedNumber} 床`" :value="bed.id" />
          </el-select>
          <span v-if="sel.roomId && !freeBeds.length" style="color: var(--dms-bad); font-size: 12px">该房间无空闲床位</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" :disabled="!sel.bedId" @click="onAssign">确认入住</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageIntakes, createIntake, assignIntake, cancelIntake } from '@/api/checkin'
import { pageResidents } from '@/api/resident'
import { pageBuildings } from '@/api/building'
import { pageRooms } from '@/api/room'
import { listBeds } from '@/api/bed'
import type { CheckinIntake, Resident, Building, Room, Bed } from '@/api/types'
import { INTAKE_STATUS, INTAKE_SOURCE, GENDER_LIMIT, ROOM_TYPE, labelOf, tagTypeOf } from '@/utils/dict'

const loading = ref(false)
const saving = ref(false)
const list = ref<CheckinIntake[]>([])
const total = ref(0)
const query = reactive({ status: undefined as number | undefined, source: undefined as number | undefined, page: 1, size: 10 })

const residents = ref<Resident[]>([])
const buildings = ref<Building[]>([])
const rooms = ref<Room[]>([])
const freeBeds = ref<Bed[]>([])

const createVisible = ref(false)
const createRef = ref<FormInstance>()
const createForm = reactive<{ residentId?: number; expectCheckinDate?: string; genderLimitReq?: number; remark?: string }>({})
const createRules = { residentId: [{ required: true, message: '请选择居住人', trigger: 'change' }] }

const assignVisible = ref(false)
const current = ref<CheckinIntake>()
const sel = reactive<{ buildingId?: number; roomId?: number; bedId?: number }>({})

async function reload() {
  loading.value = true
  try {
    const res = await pageIntakes(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
function onPageChange(p: number) { query.page = p; reload() }

async function loadResidents() {
  const res = await pageResidents({ page: 1, size: 1000, status: 1 })
  residents.value = res.records
}
function openCreate() {
  Object.assign(createForm, { residentId: undefined, expectCheckinDate: undefined, genderLimitReq: 0, remark: '' })
  createVisible.value = true
}
async function onCreate() {
  await createRef.value?.validate()
  saving.value = true
  try {
    await createIntake(createForm as { residentId: number })
    ElMessage.success('已创建意向单')
    createVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

async function openAssign(row: CheckinIntake) {
  current.value = row
  Object.assign(sel, { buildingId: undefined, roomId: undefined, bedId: undefined })
  rooms.value = []
  freeBeds.value = []
  if (!buildings.value.length) {
    const res = await pageBuildings({ page: 1, size: 100 })
    buildings.value = res.records
  }
  assignVisible.value = true
}
async function onBuildingChange() {
  sel.roomId = undefined
  sel.bedId = undefined
  freeBeds.value = []
  if (!sel.buildingId) { rooms.value = []; return }
  const res = await pageRooms({ buildingId: sel.buildingId, page: 1, size: 200 })
  rooms.value = res.records
}
async function onRoomChange() {
  sel.bedId = undefined
  if (!sel.roomId) { freeBeds.value = []; return }
  const beds = await listBeds(sel.roomId)
  freeBeds.value = beds.filter((b) => b.status === 1)
}
async function onAssign() {
  if (!current.value || !sel.bedId) return
  saving.value = true
  try {
    await assignIntake(current.value.id, { bedId: sel.bedId })
    ElMessage.success('入住成功')
    assignVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}
async function onCancel(row: CheckinIntake) {
  await ElMessageBox.confirm(`确认取消「${row.residentName}」的意向单？`, '提示', { type: 'warning' })
  await cancelIntake(row.id)
  ElMessage.success('已取消')
  reload()
}

onMounted(() => { reload(); loadResidents() })
</script>
