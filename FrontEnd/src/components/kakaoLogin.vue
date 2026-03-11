<template>
  <div></div>
</template>
<script setup>
import axiosInstance from '@/libs/httpRequester';
import { ref, onMounted, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAccountStore } from '@/stores/account';
const router = useRouter();
const route = useRoute();
const code = ref('');
const accountStore = useAccountStore();

onMounted(async () => {
  code.value = route.query.code;
  await getToken();
});

async function getToken() {
  try {
    const response = await axiosInstance.get(`/kakaologin/${code.value}`);
    const token = response.data;
    localStorage.setItem('accessToken', JSON.stringify(token));
    accountStore.setAccessToken(token);
    accountStore.setLoggedIn(true);
    accountStore.setChecked(true);

    const member = await getMyInfo(); // ✅ getMyInfo() 사용
    if (member) {
      accountStore.setUser(member); // ✅ user 정보 Pinia에 저장
    }

    router.push('/library/booking');
  } catch (error) {
    return null;
  }
}

async function getMyInfo() {
  try {
    const response = await axiosInstance.get('/account/myInfo');
    const member = response.data;
    return member;
  } catch (error) {
    return null;
  }
}
</script>
