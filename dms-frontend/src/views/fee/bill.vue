<template>
  <el-card shadow="never">
    <el-form :inline="true" :model="query" @keyup.enter="reload">
      <el-form-item label="账期">
        <el-date-picker v-model="query.period" type="month" value-format="YYYY-MM" placeholder="全部" clearable
          style="width: 140px" @change="reload" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px" @change="reload">
          <el-option v-for="s in BILL_STATUS" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="query.billType" placeholder="全部" clearable style="width: 110px" @change="reload">
          <el-option v-for="t in BILL_TYPE" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="reload">查询</el-button>
        <el-button type="primary" @click="openGenerate">生成账单</el-button>
          <el-button :loading="exporting" @click="onExport">导出</el-button>
      </el-form-item>
    </el-form>

    <div style="margin-bottom: 10px; color: var(--dms-ink-2); font-size: 13px">
      共 {{ total }} 张 · 本页：已缴 {{ paidCount }} · 未缴 {{ unpaidCount }} · 金额合计 ¥{{ amountSum.toFixed(2) }}
    </div>

    <el-table v-loading="loading" :data="list">
      <el-table-column prop="billNo" label="账单号" width="170" />
      <el-table-column label="居住人" width="150">
        <template #default="{ row }">{{ row.residentName }}（{{ row.employeeNo }}）</template>
      </el-table-column>
      <el-table-column label="房间" width="110">
        <template #default="{ row }">{{ row.roomNumber ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="period" label="账期" width="100" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">{{ labelOf(BILL_TYPE, row.billType ?? 1) }}</template>
      </el-table-column>
      <el-table-column label="金额" width="110">
        <template #default="{ row }">¥{{ Number(row.amount).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="明细" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="tagTypeOf(BILL_STATUS, row.status) as any" size="small" round>{{ labelOf(BILL_STATUS, row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paidAt" label="缴费时间" width="170" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status === 1 || row.status === 4" link type="primary" @click="openPay(row)">缴费</el-button>
          <el-button v-if="row.status === 1" link type="danger" @click="onVoid(row)">作废</el-button>
          <span v-if="row.status === 2 || row.status === 3" style="color: var(--dms-ink-2); font-size: 12px">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-if="total > query.size" style="margin-top: 12px; justify-content: flex-end"
      layout="total, prev, pager, next" :total="total" :current-page="query.page" :page-size="query.size"
      @current-change="onPageChange" />

    <!-- 生成账单 -->
    <el-dialog v-model="genVisible" title="生成账单" width="420px">
      <el-form label-width="90px">
        <el-form-item label="账期">
          <el-date-picker v-model="genPeriod" type="month" value-format="YYYY-MM" placeholder="选择账期" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onGenerate">生成</el-button>
      </template>
    </el-dialog>

    <!-- 缴费 -->
    <el-dialog v-model="payVisible" title="缴费" width="420px">
      <el-descriptions :column="1" border size="small" style="margin-bottom: 14px">
        <el-descriptions-item label="居住人">{{ current?.residentName }}（{{ current?.employeeNo }}）</el-descriptions-item>
        <el-descriptions-item label="账期">{{ current?.period }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ Number(current?.amount ?? 0).toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="90px">
        <el-form-item label="缴费方式">
          <el-select v-model="payMethod" style="width: 100%">
            <el-option v-for="m in PAY_METHOD" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onPay">确认缴费</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageBills, generateBills, payBill, voidBill } from '@/api/fee'
import type { FeeBill } from '@/api/types'
import { BILL_STATUS, BILL_TYPE, PAY_METHOD, labelOf, tagTypeOf } from '@/utils/dict'
import { exportLedger } from '@/api/export'

const loading = ref(false)
const exporting = ref(false)
const saving = ref(false)
const list = ref<FeeBill[]>([])
const total = ref(0)
const query = reactive({ period: undefined as string | undefined, status: undefined as number | undefined, billType: undefined as number | undefined, page: 1, size: 10 })

const paidCount = computed(() => list.value.filter((b) => b.status === 2).length)
const unpaidCount = computed(() => list.value.filter((b) => b.status === 1).length)
const amountSum = computed(() => list.value.reduce((s, b) => s + Number(b.amount), 0))

const genVisible = ref(false)
const genPeriod = ref<string>()

const payVisible = ref(false)
const current = ref<FeeBill>()
const payMethod = ref<number>(1)

async function reload() {
  loading.value = true
  try {
    const res = await pageBills(query)
    list.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
function onPageChange(p: number) { query.page = p; reload() }

function openGenerate() {
  genPeriod.value = undefined
  genVisible.value = true
}
async function onGenerate() {
  if (!genPeriod.value) { ElMessage.warning('请选择账期'); return }
  saving.value = true
  try {
    const r = await generateBills({ period: genPeriod.value })
    ElMessage.success(`生成 ${r.generated} 张 / 跳过 ${r.skipped} 张`)
    genVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}

function openPay(row: FeeBill) {
  current.value = row
  payMethod.value = 1
  payVisible.value = true
}
async function onPay() {
  if (!current.value) return
  saving.value = true
  try {
    await payBill(current.value.id, { payMethod: payMethod.value })
    ElMessage.success('缴费成功')
    payVisible.value = false
    reload()
  } finally {
    saving.value = false
  }
}
async function onVoid(row: FeeBill) {
  await ElMessageBox.confirm(`确认作废「${row.billNo}」？`, '提示', { type: 'warning' })
  await voidBill(row.id)
  ElMessage.success('已作废')
  reload()
}

async function onExport() {
  exporting.value = true
  try {
    await exportLedger('fee-bills', { ...query })
  } finally {
    exporting.value = false
  }
}

onMounted(reload)
</script>
