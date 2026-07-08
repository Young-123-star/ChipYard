<template>
  <el-card shadow="never">
    <el-form :inline="true" :model="query">
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px" @change="reload">
          <el-option v-for="s in CHECKOUT_STATUS" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源">
        <el-select v-model="query.source" placeholder="全部" clearable style="width: 120px" @change="reload">
          <el-option v-for="s in CHECKOUT_SOURCE" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="reload">查询</el-button>
        <el-button type="success" @click="openCreate">手工新建</el-button>
          <el-button :loading="exporting" @click="onExport">导出</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column prop="bizNo" label="业务号" width="170" />
      <el-table-column label="居住人" width="160">
        <template #default="{ row }">{{ row.residentName }}（{{ row.employeeNo }}）</template>
      </el-table-column>
      <el-table-column label="来源" width="100">
        <template #default="{ row }">{{ labelOf(CHECKOUT_SOURCE, row.source) }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" />
      <el-table-column prop="expectCheckoutDate" label="预计退宿" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="tagTypeOf(CHECKOUT_STATUS, row.status) as any" size="small" round>{{ labelOf(CHECKOUT_STATUS, row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="离场欠费" width="110">
        <template #default="{ row }">
          <span v-if="row.arrearsAmount && row.arrearsAmount > 0" style="color: var(--el-color-danger)">¥{{ Number(row.arrearsAmount).toFixed(2) }}</span>
          <span v-else style="color: var(--dms-ink-2)">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <template v-if="row.status === 1">
            <el-button link type="primary" @click="openConfirm(row)">办理退宿</el-button>
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
    <el-dialog v-model="createVisible" title="手工新建退宿单" width="460px">
      <el-form ref="createRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="居住人" prop="residentId">
          <el-select v-model="createForm.residentId" filterable placeholder="选择在住居住人" style="width: 100%">
            <el-option v-for="r in activeResidents" :key="r.residentId" :label="`${r.residentName}（${r.employeeNo}）`" :value="r.residentId" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计退宿"><el-date-picker v-model="createForm.expectCheckoutDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="createForm.reason" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 办理退宿 -->
    <el-dialog v-model="confirmVisible" title="办理退宿" width="460px">
      <el-descriptions :column="1" border size="small" style="margin-bottom: 14px">
        <el-descriptions-item label="居住人">{{ current?.residentName }}（{{ current?.employeeNo }}）</el-descriptions-item>
        <el-descriptions-item label="所退房间/床位">房间 {{ current?.roomId ?? '-' }} / 床位 {{ current?.bedId ?? '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-alert v-if="arrears.count > 0" type="warning" :closable="false" show-icon style="margin-bottom: 14px"
        :title="`待结算欠费：${arrears.count} 张 / ¥${Number(arrears.totalAmount).toFixed(2)}`"
        description="确认退宿将把以上未缴账单挂账。" />
      <el-alert v-else-if="arrearsLoaded" type="success" :closable="false" show-icon style="margin-bottom: 14px" title="无待结算欠费" />
      <el-alert v-else type="info" :closable="false" show-icon style="margin-bottom: 14px" title="欠费信息加载失败，请确认后重试" />
      <el-form label-width="90px">
        <el-form-item label="退宿日期"><el-date-picker v-model="checkoutDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onConfirm">确认退宿</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { pageCheckoutOrders, createCheckoutOrder, confirmCheckout, cancelCheckout } from '@/api/checkout'
import { getArrears } from '@/api/fee'
import { pageRecords } from '@/api/checkin'
import type { CheckoutOrder, CheckinRecord } from '@/api/types'
import { CHECKOUT_STATUS, CHECKOUT_SOURCE, labelOf, tagTypeOf } from '@/utils/dict'
import { exportLedger } from '@/api/export'

const loading = ref(false)
const exporting = ref(false)
const saving = ref(false)
const list = ref<CheckoutOrder[]>([])
const total = ref(0)
const query = reactive({ status: undefined as number | undefined, source: undefined as number | undefined, page: 1, size: 10 })

const activeResidents = ref<CheckinRecord[]>([])
const createVisible = ref(false)
const createRef = ref<FormInstance>()
const createForm = reactive<{ residentId?: number; reason?: string; expectCheckoutDate?: string }>({})
const createRules = { residentId: [{ required: true, message: '请选择居住人', trigger: 'change' }] }

const confirmVisible = ref(false)
const current = ref<CheckoutOrder>()
const checkoutDate = ref<string>()
const arrears = ref<{ count: number; totalAmount: number }>({ count: 0, totalAmount: 0 })
const arrearsLoaded = ref(false)

async function reload() {
  loading.value = true
  try {
    const res = await pageCheckoutOrders(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
function onPageChange(p: number) { query.page = p; reload() }

async function loadActiveResidents() {
  const res = await pageRecords({ status: 1, page: 1, size: 1000 })
  activeResidents.value = res.records
}
function openCreate() {
  Object.assign(createForm, { residentId: undefined, reason: '', expectCheckoutDate: undefined })
  createVisible.value = true
}
async function onCreate() {
  await createRef.value?.validate()
  saving.value = true
  try {
    await createCheckoutOrder(createForm as { residentId: number })
    ElMessage.success('已创建退宿单')
    createVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

async function openConfirm(row: CheckoutOrder) {
  current.value = row
  checkoutDate.value = undefined
  arrears.value = { count: 0, totalAmount: 0 }
  arrearsLoaded.value = false
  confirmVisible.value = true
  if (row.checkinRecordId) {
    try {
      arrears.value = await getArrears(row.checkinRecordId)
      arrearsLoaded.value = true
    } catch {
      arrearsLoaded.value = false
    }
  } else {
    arrearsLoaded.value = true
  }
}
async function onConfirm() {
  if (!current.value) return
  saving.value = true
  try {
    await confirmCheckout(current.value.id, { checkoutDate: checkoutDate.value })
    ElMessage.success('退宿成功')
    confirmVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}
async function onCancel(row: CheckoutOrder) {
  await ElMessageBox.confirm(`确认取消「${row.residentName}」的退宿单？`, '提示', { type: 'warning' })
  await cancelCheckout(row.id)
  ElMessage.success('已取消')
  reload()
}

async function onExport() {
  exporting.value = true
  try {
    await exportLedger('checkout-orders', { ...query })
  } finally {
    exporting.value = false
  }
}

onMounted(() => { reload(); loadActiveResidents() })
</script>
