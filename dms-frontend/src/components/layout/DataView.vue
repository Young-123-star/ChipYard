<template>
  <section class="data-view">
    <div v-if="hasFilters" class="data-view__filters">
      <div class="data-view__fields" :class="{ 'is-collapsed': collapsible && !expanded }"><slot name="filters" /></div>
      <div class="data-view__filter-actions">
        <slot name="filter-actions" />
        <el-button v-if="collapsible" link type="primary" class="data-view__toggle" @click="expanded = !expanded">
          {{ expanded ? '收起筛选' : '更多筛选' }}
          <el-icon><ArrowUp v-if="expanded" /><ArrowDown v-else /></el-icon>
        </el-button>
        <slot v-if="$slots.actions" name="actions" />
      </div>
    </div>
    <div class="data-view__toolbar">
      <div>
        <h2>{{ title }}</h2>
        <span v-if="total !== undefined" class="data-view__count">共 {{ total }} 条</span>
      </div>
      <div v-if="$slots.actions && !hasFilters" class="data-view__actions"><slot name="actions" /></div>
    </div>
    <div class="data-view__content"><slot /></div>
    <div v-if="$slots.pagination" class="data-view__pagination"><slot name="pagination" /></div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, useSlots } from 'vue'
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue'

// collapsible：筛选区默认收起为一行，点“更多筛选”展开全部
defineProps<{ title: string; total?: number; collapsible?: boolean }>()
const expanded = ref(false)
const slots = useSlots()
// 有筛选区时，主操作按钮（新增/导出）并入筛选行右侧；无筛选区时回到工具栏
const hasFilters = computed(() => !!(slots.filters || slots['filter-actions']))
</script>

<style scoped>
.data-view { overflow: hidden; border: 1px solid var(--dms-hairline); border-radius: var(--dms-radius-card); background: var(--dms-surface); box-shadow: var(--dms-shadow-card); }
.data-view__filters { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; padding: 12px 18px; border-bottom: 1px solid var(--dms-hairline); background: #fafafa; }
.data-view__fields { display: flex; flex: 1; flex-wrap: wrap; align-items: flex-end; gap: 12px; min-width: 0; }
.data-view__fields.is-collapsed { max-height: 34px; overflow: hidden; }
.data-view__filter-actions, .data-view__actions { display: flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.data-view__toggle { white-space: nowrap; }
.data-view__toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; min-height: 56px; padding: 0 18px; }
.data-view__toolbar > div:first-child { display: flex; align-items: baseline; gap: 10px; }
h2 { margin: 0; font-size: 15px; letter-spacing: -.015em; }
.data-view__count { color: var(--dms-ink-2); font-size: 12.5px; font-variant-numeric: tabular-nums; }
.data-view__content { min-width: 0; padding: 0 18px 18px; }
.data-view__pagination { display: flex; justify-content: flex-end; padding: 0 18px 18px; }
@media (max-width: 767px) {
  .data-view__filters { align-items: stretch; flex-direction: column; padding: 14px; }
  .data-view__filter-actions { justify-content: flex-end; }
  .data-view__toolbar { padding: 0 14px; }
  .data-view__content { overflow-x: auto; padding: 0 14px 14px; }
  .data-view__pagination { overflow-x: auto; justify-content: flex-start; padding: 0 14px 14px; }
}
</style>
