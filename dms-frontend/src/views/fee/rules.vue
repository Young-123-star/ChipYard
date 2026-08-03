<template>
  <div class="rules-page">
    <!-- 计费参数 -->
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="panel__head">
          <div><b>计费参数</b><span>修改仅影响之后生成的结算，历史结算不回算</span></div>
          <el-button type="primary" :loading="savingRate" @click="onSaveRate">保存参数</el-button>
        </div>
      </template>
      <el-form v-loading="loadingRate" class="rate-form" label-width="165px" label-position="left">
        <div class="rate-grid">
          <el-form-item label="电价（元/度）"><el-input-number v-model="rateForm.electricityPrice" :min="0" :precision="4" :step="0.0001" /></el-form-item>
          <el-form-item label="水价（元/吨）"><el-input-number v-model="rateForm.waterPrice" :min="0" :precision="4" :step="0.0001" /></el-form-item>
          <el-form-item label="电免额（度/间/月）"><el-input-number v-model="rateForm.electricAllowance" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="户级水免额（吨/户/月）"><el-input-number v-model="rateForm.householdWaterAllowance" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="房间水免额（吨/间/月）"><el-input-number v-model="rateForm.roomWaterAllowance" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="结算周期">
            <div class="cycle-ctl">
              上月<el-input-number v-model="rateForm.cycleStartDay" :min="1" :max="28" :precision="0" controls-position="right" class="day" />日
              至本月<el-input-number v-model="rateForm.cycleEndDay" :min="1" :max="28" :precision="0" controls-position="right" class="day" />日
            </div>
          </el-form-item>
        </div>
      </el-form>
    </el-card>

    <!-- 计算规则说明 -->
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="panel__head">
          <div><b>计算规则说明</b><span>免额与单价引用上方当前配置值；结算周期上月 {{ cfg.cycleStartDay }} 日 至 本月 {{ cfg.cycleEndDay }} 日，以周期截止日当天在住人数为准（截止日入住计入，之后入住或截止前退宿不计入）</span></div>
        </div>
      </template>
      <div class="rule-groups">
        <section v-for="group in ruleGroups" :key="group.title" class="rule-group">
          <h3>{{ group.title }}</h3>
          <div class="rule-cards">
            <div v-for="doc in group.docs" :key="doc.value" class="rule-card">
              <div class="rule-card__head">
                <span class="rule-card__name">{{ doc.name }}</span>
                <span class="rule-card__mode">{{ doc.mode }}</span>
              </div>
              <dl>
                <div><dt>抄表</dt><dd>{{ doc.metering }}</dd></div>
                <div><dt>免额</dt><dd>{{ doc.allowance }}</dd></div>
                <div><dt>费用与分摊</dt><dd>{{ doc.sharing }}</dd></div>
              </dl>
            </div>
          </div>
        </section>
      </div>
    </el-card>

    <!-- 账户规则配置 -->
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="panel__head">
          <div><b>账户规则配置</b><span>按结算账户批量配置，同户内所有房间的方式与规则必须一致</span></div>
          <el-button type="primary" @click="openBatch">批量配置</el-button>
        </div>
      </template>
      <el-table v-loading="loadingAccounts" :data="accounts">
        <el-table-column label="楼栋" width="150"><template #default="{ row }">{{ buildingName(row.buildingId) }}</template></el-table-column>
        <el-table-column prop="accountCode" label="账户编码" width="130" />
        <el-table-column label="方式" width="100"><template #default="{ row }">{{ labelOf(SETTLEMENT_MODE, row.settlementMode) }}</template></el-table-column>
        <el-table-column label="包含房间" min-width="180"><template #default="{ row }">{{ row.roomNumbers.join('、') }}</template></el-table-column>
        <el-table-column label="用电规则" min-width="120"><template #default="{ row }">{{ labelOf(ELECTRIC_RULE, row.electricityRule) }}</template></el-table-column>
        <el-table-column label="用水规则" min-width="120"><template #default="{ row }">{{ labelOf(WATER_RULE, row.waterRule) }}</template></el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.configured && !row.errors.length ? 'success' : 'warning'">{{ row.configured && !row.errors.length ? '已配置' : '待完善' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }"><el-button link type="primary" @click="openEdit(row)">编辑</el-button></template>
        </el-table-column>
        <el-table-column label="校验提示" min-width="180">
          <template #default="{ row }"><span class="err-text">{{ row.errors.join('；') || '-' }}</span></template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 账户编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑账户规则" width="480px">
      <el-form label-width="100px">
        <el-form-item label="楼栋 / 账户">
          <span>{{ editing ? `${buildingName(editing.buildingId)} · ${editing.accountCode}` : '-' }}</span>
        </el-form-item>
        <el-form-item label="包含房间">
          <span>{{ editing?.roomNumbers.join('、') }}</span>
        </el-form-item>
        <el-form-item label="结算方式">
          <el-select v-model="editForm.settlementMode" style="width: 100%">
            <el-option v-for="m in SETTLEMENT_MODE" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用电规则">
          <el-select v-model="editForm.electricityRule" style="width: 100%">
            <el-option v-for="r in ELECTRIC_RULE" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用水规则">
          <el-select v-model="editForm.waterRule" style="width: 100%">
            <el-option v-for="r in WATER_RULE" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-alert v-if="editForm.electricityRule === 0 && editForm.waterRule === 0" type="warning" :closable="false" title="水电规则不能都为「不计」" />
        <el-alert v-else-if="(editForm.electricityRule === 1 || editForm.waterRule === 1) && editForm.settlementMode !== 1" type="warning" :closable="false" title="户级规则要求结算方式为「户级账户」" />
        <el-alert v-else-if="(editForm.electricityRule === 3 || editForm.waterRule === 3) && (editing?.roomIds.length ?? 0) > 1" type="warning" :closable="false" title="夫妻房规则要求单房间账户" />
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingAccount" @click="onSaveAccount">保存</el-button>
      </template>
    </el-dialog>
    <!-- 批量配置弹窗 -->
    <el-dialog v-model="batchVisible" title="批量刷新水电配置" width="480px">
      <el-form label-width="100px">
        <el-form-item label="楼栋范围">
          <el-select v-model="batchForm.buildingId" clearable placeholder="全部楼栋" style="width: 100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房型范围">
          <el-select v-model="batchForm.roomType" clearable placeholder="全部房型" style="width: 100%">
            <el-option v-for="t in ROOM_TYPE" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="影响范围">
          <span class="batch-count">将更新 <b>{{ batchAffected }}</b> 个房间</span>
        </el-form-item>
        <el-form-item label="结算方式">
          <el-select v-model="batchForm.settlementMode" style="width: 100%">
            <el-option v-for="m in SETTLEMENT_MODE" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用电规则">
          <el-select v-model="batchForm.electricityRule" style="width: 100%">
            <el-option v-for="r in ELECTRIC_RULE" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="用水规则">
          <el-select v-model="batchForm.waterRule" style="width: 100%">
            <el-option v-for="r in WATER_RULE" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-alert v-if="batchForm.electricityRule === 0 && batchForm.waterRule === 0" type="warning" :closable="false" title="水电规则不能都为「不计」" />
        <el-alert v-else-if="(batchForm.electricityRule === 1 || batchForm.waterRule === 1) && batchForm.settlementMode !== 1" type="warning" :closable="false" title="户级规则要求结算方式为「户级账户」" />
        <el-alert v-else-if="(batchForm.electricityRule === 3 || batchForm.waterRule === 3) && batchForm.settlementMode !== 2" type="warning" :closable="false" title="夫妻房规则仅适用单房间账户" />
        <el-alert v-else-if="batchForm.settlementMode === 1" type="info" :closable="false" title="户级模式将沿用各房间现有账户编码自动并户；没有账户编码的房间会被跳过" />
        <el-alert v-else type="info" :closable="false" title="房间账户模式下每个房间独立成户（账户编码取房间号）" />
      </el-form>
      <template #footer>
        <el-button @click="batchVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!batchAffected || batchInvalid" :loading="savingBatch" @click="onBatchApply">确认应用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { batchApplyUtilityAccount, getUtilityRate, listUtilityAccounts, saveUtilityAccount, updateUtilityRate } from '@/api/fee'
import { pageBuildings } from '@/api/building'
import { pageRooms } from '@/api/room'
import type { Building, Room, UtilityAccount, UtilityRate } from '@/api/types'
import { ELECTRIC_RULE, ROOM_TYPE, SETTLEMENT_MODE, WATER_RULE, labelOf } from '@/utils/dict'

const buildings = ref<Building[]>([])
const accounts = ref<UtilityAccount[]>([])
const rooms = ref<Room[]>([])
const loadingRate = ref(false)
const loadingAccounts = ref(false)
const savingRate = ref(false)
const savingAccount = ref(false)

// 配置未加载完成前的兜底展示值（与后端默认值一致）
const cfg = reactive({ electricityPrice: 0.5383, waterPrice: 4.15, electricAllowance: 250, householdWaterAllowance: 50, roomWaterAllowance: 17, cycleStartDay: 25, cycleEndDay: 24 })
const rateForm = reactive({ ...cfg })

interface RuleDoc { value: number; name: string; mode: string; metering: string; allowance: string; sharing: string }

// 规则说明数据：免额/单价引用当前配置动态生成
const ruleGroups = computed(() => [
  {
    title: '用电规则',
    docs: [
      { value: 0, name: '不计电费', mode: '任意账户', metering: '不抄电表', allowance: '—', sharing: '电分支整体跳过，不产生电费' },
      { value: 1, name: '户级总表分摊', mode: '户级账户（多房间）', metering: '户总电表 + 每房间电表', allowance: `${cfg.electricAllowance} 度/间/月`, sharing: '公摊电量 = 户总表 − Σ房间表；房间实际电量 = 房间表 + 公摊÷房间数；超出免额部分×电价由在住人均摊，空房间费用由公司承担' },
      { value: 2, name: '房间表计量', mode: '单房间账户', metering: '仅房间电表', allowance: `${cfg.electricAllowance} 度/间/月`, sharing: '超出免额部分×电价由在住人均摊，空房间费用由公司承担' },
      { value: 3, name: '夫妻房均摊', mode: '单房间账户', metering: '仅房间电表', allowance: '无免额', sharing: '结算时须恰好 2 名在住，全部费用 2 人均摊' },
      { value: 4, name: '公司承担', mode: '单房间账户', metering: '仅房间电表', allowance: '—', sharing: '照实计费，费用全部公司承担，不出个人账单' }
    ] as RuleDoc[]
  },
  {
    title: '用水规则',
    docs: [
      { value: 0, name: '不计水费', mode: '任意账户', metering: '不抄水表', allowance: '—', sharing: '水分支整体跳过，不产生水费' },
      { value: 1, name: '户级总表分摊', mode: '户级账户（多房间）', metering: '户总冷水表 + 热水表（不抄房间表）', allowance: `${cfg.householdWaterAllowance} 吨/户/月`, sharing: '房间用水量 = 户总量÷房间数；超出免额部分按房间数均摊后由在住人承担，空房份额公司承担' },
      { value: 2, name: '房间表计量', mode: '单房间账户', metering: '房间冷水表 + 热水表', allowance: `${cfg.roomWaterAllowance} 吨/间/月`, sharing: '冷热水合计超出免额部分×水价由在住人均摊，空房间费用由公司承担' },
      { value: 3, name: '夫妻房均摊', mode: '单房间账户', metering: '房间冷水表 + 热水表', allowance: '无免额', sharing: '结算时须恰好 2 名在住，全部费用 2 人均摊' },
      { value: 4, name: '公司承担', mode: '单房间账户', metering: '房间冷水表 + 热水表', allowance: '—', sharing: '照实计费，费用全部公司承担，不出个人账单' }
    ] as RuleDoc[]
  }
])

function buildingName(id: number) { return buildings.value.find(b => b.id === id)?.buildingName || id }

async function loadAll() {
  loadingRate.value = true
  loadingAccounts.value = true
  try {
    const [rate, acc, bld, rm] = await Promise.all([
      getUtilityRate().catch(() => undefined),
      listUtilityAccounts().catch(() => [] as UtilityAccount[]),
      pageBuildings({ page: 1, size: 1000 }).catch(() => ({ records: [] as Building[], total: 0 })),
      pageRooms({ page: 1, size: 1000 }).catch(() => ({ records: [] as Room[], total: 0 }))
    ])
    if (rate) {
      // 后端老版本无新字段时兜底默认值
      Object.assign(cfg, {
        electricityPrice: rate.electricityPrice,
        waterPrice: rate.waterPrice,
        electricAllowance: rate.electricAllowance ?? 250,
        householdWaterAllowance: rate.householdWaterAllowance ?? 50,
        roomWaterAllowance: rate.roomWaterAllowance ?? 17,
        cycleStartDay: rate.cycleStartDay ?? 25,
        cycleEndDay: rate.cycleEndDay ?? 24
      })
      Object.assign(rateForm, cfg)
    }
    accounts.value = acc
    buildings.value = bld.records
    rooms.value = rm.records
  } finally {
    loadingRate.value = false
    loadingAccounts.value = false
  }
}

async function onSaveRate() {
  savingRate.value = true
  try {
    await updateUtilityRate({ ...rateForm })
    Object.assign(cfg, rateForm)
    ElMessage.success('计费参数已保存，之后的结算将按新参数计算')
  } finally {
    savingRate.value = false
  }
}

const editVisible = ref(false)
const editing = ref<UtilityAccount>()
const editForm = reactive({ settlementMode: 1, electricityRule: 0, waterRule: 0 })

function openEdit(row: UtilityAccount) {
  editing.value = row
  Object.assign(editForm, { settlementMode: row.settlementMode, electricityRule: row.electricityRule, waterRule: row.waterRule })
  editVisible.value = true
}

async function onSaveAccount() {
  if (!editing.value) return
  savingAccount.value = true
  try {
    await saveUtilityAccount({
      buildingId: editing.value.buildingId,
      accountCode: editing.value.accountCode,
      settlementMode: editForm.settlementMode,
      electricityRule: editForm.electricityRule,
      waterRule: editForm.waterRule,
      roomIds: editing.value.roomIds
    })
    ElMessage.success('账户规则已保存')
    editVisible.value = false
    accounts.value = await listUtilityAccounts()
  } finally {
    savingAccount.value = false
  }
}

// 批量刷新水电配置
const batchVisible = ref(false)
const savingBatch = ref(false)
const batchForm = reactive({ buildingId: undefined as number | undefined, roomType: undefined as number | undefined, settlementMode: 2, electricityRule: 2, waterRule: 2 })

// 受影响房间数：楼栋/房型筛选后的匹配数
const batchAffected = computed(() => rooms.value.filter(r =>
  (!batchForm.buildingId || r.buildingId === batchForm.buildingId) &&
  (!batchForm.roomType || r.roomType === batchForm.roomType)
).length)
const batchInvalid = computed(() =>
  (batchForm.electricityRule === 0 && batchForm.waterRule === 0) ||
  ((batchForm.electricityRule === 1 || batchForm.waterRule === 1) && batchForm.settlementMode !== 1) ||
  ((batchForm.electricityRule === 3 || batchForm.waterRule === 3) && batchForm.settlementMode !== 2)
)

function openBatch() { batchVisible.value = true }

async function onBatchApply() {
  try {
    await ElMessageBox.confirm(
      `将把筛选范围内 ${batchAffected.value} 个房间的计费方式更新为所选规则，确认执行？`,
      '批量刷新确认',
      { type: 'warning', confirmButtonText: '确认应用', cancelButtonText: '取消' }
    )
  } catch { return }
  savingBatch.value = true
  try {
    const res = await batchApplyUtilityAccount({
      buildingId: batchForm.buildingId,
      roomType: batchForm.roomType,
      settlementMode: batchForm.settlementMode,
      electricityRule: batchForm.electricityRule,
      waterRule: batchForm.waterRule
    })
    ElMessage.success(`已更新 ${res.updated} 个房间${res.skipped ? `，${res.skipped} 个因缺少账户编码被跳过` : ''}`)
    batchVisible.value = false
    accounts.value = await listUtilityAccounts()
  } finally {
    savingBatch.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.rules-page { display: grid; gap: 14px; }
.panel :deep(.el-card__header) { padding: 14px 18px; }
.panel :deep(.el-card__body) { padding: 16px 18px; }
.panel__head { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
.panel__head div > * { display: block; }
.panel__head b { font-size: 15px; }
.panel__head span { margin-top: 3px; color: var(--dms-ink-2); font-size: 12px; font-weight: 400; }

.rate-form :deep(.el-form-item) { margin-bottom: 10px; }
.rate-form :deep(.el-form-item__label) { white-space: nowrap; }
.rate-grid { display: grid; grid-template-columns: repeat(3, minmax(300px, 1fr)); column-gap: 24px; }
.cycle-ctl { display: flex; align-items: center; gap: 4px; white-space: nowrap; color: var(--dms-ink-2); font-size: 12.5px; }
.cycle-ctl .day { width: 78px; }

.rule-groups { display: grid; gap: 18px; }
.rule-group h3 { margin: 0 0 10px; font-size: 13.5px; color: var(--dms-ink-2); }
.rule-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 10px; }
.rule-card { padding: 13px 14px; border: 1px solid var(--dms-hairline); border-radius: var(--dms-radius-card); background: var(--dms-surface); }
.rule-card__head { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 8px; }
.rule-card__name { font-size: 13.5px; font-weight: 650; }
.rule-card__mode { font-size: 11px; color: var(--dms-accent-ink); background: var(--dms-accent-soft); border-radius: 6px; padding: 2px 8px; white-space: nowrap; }
.rule-card dl { margin: 0; display: grid; gap: 5px; }
.rule-card dl > div { display: grid; grid-template-columns: 64px 1fr; gap: 8px; font-size: 12px; line-height: 1.6; }
.rule-card dt { color: var(--dms-ink-3); }
.rule-card dd { margin: 0; color: var(--dms-ink-2); }

.err-text { color: var(--dms-warn); font-size: 12px; }
.batch-count { color: var(--dms-ink-2); font-size: 12.5px; }
.batch-count b { color: var(--dms-accent-ink); font-variant-numeric: tabular-nums; }
@media (max-width: 1199px) { .rate-grid { grid-template-columns: repeat(2, minmax(300px, 1fr)); } }
@media (max-width: 767px) { .rate-grid { grid-template-columns: 1fr; } }
</style>
