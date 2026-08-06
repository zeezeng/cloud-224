<script setup lang="ts">
import { rankingRecords } from '@/data/ephone'
import YunPage from '@/components/ephone/YunPage.vue'
import PodiumBoard from '@/components/ephone/PodiumBoard.vue'
import RankingList from '@/components/ephone/RankingList.vue'

defineOptions({
  name: 'Ranking',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '排行',
  },
})
</script>

<template>
  <YunPage title="主播排行" subtitle="每一份热爱都值得被看见">
    <PodiumBoard :records="rankingRecords" />
    <RankingList :records="rankingRecords.slice(3, 8)" value-label="" show-trend :start-rank="4" />
  </YunPage>
</template>
