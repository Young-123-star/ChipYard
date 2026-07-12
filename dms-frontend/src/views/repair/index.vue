<template>
  <el-card shadow='never'>
    <el-form :inline='true' :model='query' @keyup.enter='reload'>
      <el-form-item label='状态'>
        <el-select v-model='query.status' placeholder='全部' clearable style='width: 130px' @change='reload'>
          <el-option v-for='s in REPAIR_STATUS' :key='s.value' :label='s.label' :value='s.value' />
        </el-select>
      </el-form-item>
      <el-form-item label='紧急程度'>
        <el-select v-model='query.priority' placeholder='全部' clearable style='width: 130px' @change='reload'>
          <el-option v-for='p in REPAIR_PRIORITY' :key='p.value' :label='p.label' :value='p.value' />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click='reload'>查询</el-button>
        <el-button type='primary' @click='openCreate'>新建工单</el-button>
          <el-button :loading="exporting" @click="onExport">导出</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading='loading' :data='list'>
      <el-table-column prop='orderNo' label='工单号' width='150' />
      <el-table-column label='房间' width='160'>
        <template #default='{ row }'>{{ row.buildingName || '-' }} / {{ row.roomNumber || row.roomId }}</template>
      </el-table-column>
      <el-table-column prop='residentName' label='报修人' width='100' />
      <el-table-column prop='title' label='故障简述' />
      <el-table-column label='紧急程度' width='100'>
        <template #default='{ row }'><el-tag :type='tagTypeOf(REPAIR_PRIORITY, row.priority) as any'>{{ labelOf(REPAIR_PRIORITY, row.priority) }}</el-tag></template>
      </el-table-column>
      <el-table-column label='状态' width='100'>
        <template #default='{ row }'><el-tag :type='tagTypeOf(REPAIR_STATUS, row.status) as any'>{{ labelOf(REPAIR_STATUS, row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop='handler' label='处理人' width='120' />
      <el-table-column label='操作' width='180'>
        <template #default='{ row }'>
          <el-button v-if='row.status === 1' link type='primary' @click='openAccept(row)'>受理</el-button>
          <el-button v-if='row.status === 2' link type='success' @click='openComplete(row)'>完成</el-button>
          <el-button v-if='row.status === 1 || row.status === 2' link type='danger' @click='onCancel(row)'>取消</el-button>
          <span v-if='row.status === 3 || row.status === 4' style='color: var(--dms-ink-2); font-size: 12px'>-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if='total > query.size' style='margin-top: 12px; justify-content: flex-end'
      layout='total, prev, pager, next' :total='total' :current-page='query.page' :page-size='query.size'
      @current-change='onPageChange' />

    <el-dialog v-model='createVisible' title='新建维修工单' width='520px'>
      <el-form ref='createRef' :model='createForm' :rules='createRules' label-width='90px'>
        <el-form-item label='房间' prop='roomCode'><el-input v-model='createForm.roomCode' placeholder='房间号或ID' style='width: 100%' /></el-form-item>
        <el-form-item label='报修人'><el-input v-model='createForm.residentCode' placeholder='工号或ID' style='width: 100%' /></el-form-item>
        <el-form-item label='故障简述' prop='title'><el-input v-model='createForm.title' /></el-form-item>
        <el-form-item label='紧急程度'><el-select v-model='createForm.priority' style='width: 100%'><el-option v-for='p in REPAIR_PRIORITY' :key='p.value' :label='p.label' :value='p.value' /></el-select></el-form-item>
        <el-form-item label='描述'><el-input v-model='createForm.description' type='textarea' /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click='createVisible = false'>取消</el-button>
        <el-button type='primary' :loading='saving' @click='onCreate'>保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model='acceptVisible' title='受理工单' width='420px'>
      <el-input v-model='handler' placeholder='处理人' />
      <template #footer><el-button @click='acceptVisible = false'>取消</el-button><el-button type='primary' :loading='saving' @click='onAccept'>受理</el-button></template>
    </el-dialog>

    <el-dialog v-model='completeVisible' title='完成工单' width='460px'>
      <el-input v-model='result' type='textarea' placeholder='处理结果' />
      <template #footer><el-button @click='completeVisible = false'>取消</el-button><el-button type='primary' :loading='saving' @click='onComplete'>完成</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang='ts'>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageRepairOrders, createRepairOrder, acceptRepairOrder, completeRepairOrder, cancelRepairOrder } from '@/api/repair'
import type { RepairOrder } from '@/api/types'
import { REPAIR_STATUS, REPAIR_PRIORITY, labelOf, tagTypeOf } from '@/utils/dict'
import { exportLedger } from '@/api/export'

const loading = ref(false)
const exporting = ref(false)
const saving = ref(false)
const list = ref<RepairOrder[]>([])
const total = ref(0)
const query = reactive({ status: undefined as number | undefined, priority: undefined as number | undefined, page: 1, size: 10 })

const createVisible = ref(false)
const createRef = ref<FormInstance>()
const createForm = reactive<{ roomCode?: string; residentCode?: string; title?: string; description?: string; priority?: number }>({ priority: 1 })
const createRules = { roomCode: [{ required: true, message: '请输入房间号或ID', trigger: 'blur' }], title: [{ required: true, message: '请输入故障简述', trigger: 'blur' }] }

const current = ref<RepairOrder>()
const acceptVisible = ref(false)
const completeVisible = ref(false)
const handler = ref('')
const result = ref('')

async function reload() {
  loading.value = true
  try {
    const res = await pageRepairOrders(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
function onPageChange(page: number) { query.page = page; reload() }
function openCreate() { Object.assign(createForm, { roomCode: '', residentCode: '', title: '', description: '', priority: 1 }); createVisible.value = true }
async function onCreate() {
  await createRef.value?.validate()
  saving.value = true
  try { await createRepairOrder(createForm as any); ElMessage.success('已新建'); createVisible.value = false; reload() } finally { saving.value = false }
}
function openAccept(row: RepairOrder) { current.value = row; handler.value = ''; acceptVisible.value = true }
async function onAccept() {
  if (!current.value || !handler.value) return
  saving.value = true
  try { await acceptRepairOrder(current.value.id, { handler: handler.value }); ElMessage.success('已受理'); acceptVisible.value = false; reload() } finally { saving.value = false }
}
function openComplete(row: RepairOrder) { current.value = row; result.value = ''; completeVisible.value = true }
async function onComplete() {
  if (!current.value || !result.value) return
  saving.value = true
  try { await completeRepairOrder(current.value.id, { result: result.value }); ElMessage.success('已完成'); completeVisible.value = false; reload() } finally { saving.value = false }
}
async function onCancel(row: RepairOrder) {
  await ElMessageBox.confirm('确认取消该维修工单？', '提示', { type: 'warning' })
  await cancelRepairOrder(row.id)
  ElMessage.success('已取消')
  reload()
}

async function onExport() {
  exporting.value = true
  try {
    await exportLedger('repair-orders', { ...query })
  } finally {
    exporting.value = false
  }
}

onMounted(reload)
</script>