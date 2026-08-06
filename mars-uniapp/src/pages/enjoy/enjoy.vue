<script setup lang="ts">
import { enjoyRanks } from '@/data/ephone'
import EphonePage from '@/components/ephone/EphonePage.vue'
import NeonSearch from '@/components/ephone/NeonSearch.vue'
import RankingList from '@/components/ephone/RankingList.vue'

defineOptions({
  name: 'Enjoy',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '乐享值',
  },
})
</script>

<template>
  <EphonePage title="乐享值" subtitle="善意发光 · 陪伴有声">
    <NeonSearch placeholder="搜索用户昵称/用户ID" show-button variant="enjoy" />

    <RankingList :records="enjoyRanks.slice(0, 5)" value-label="乐享值" :show-rank="false" variant="enjoy" />
  </EphonePage>
</template>
