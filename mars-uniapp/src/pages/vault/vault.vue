<script setup lang="ts">
import type { EphoneVaultRecord } from '@/data/ephone'
import { vaultRecords } from '@/data/ephone'
import EphonePage from '@/components/ephone/EphonePage.vue'
import NeonSearch from '@/components/ephone/NeonSearch.vue'
import VaultCard from '@/components/ephone/VaultCard.vue'

defineOptions({
  name: 'Vault',
})

definePage({
  style: {
    navigationStyle: 'custom',
    navigationBarTitleText: '金库',
  },
})

function goVaultDetail(record: EphoneVaultRecord) {
  uni.navigateTo({
    url: `/pages/vault/detail?id=${record.id}`,
  })
}
</script>

<template>
  <EphonePage title="主播金库" subtitle="稳稳积累 · 闪耀前行">
    <NeonSearch placeholder="搜索主播/直播间号" show-button variant="vault" />

    <VaultCard
      v-for="record in vaultRecords"
      :key="record.id"
      :record="record"
      @view="goVaultDetail"
    />
  </EphonePage>
</template>
