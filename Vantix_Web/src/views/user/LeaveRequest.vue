<script setup>
import { ref, onMounted } from "vue";
import leaveService from "@/services/leaveservice.service.js";
import leaveTypeService from "@/services/leavetypeservice.service.js";

const leaveTypes = ref([]);

const form = ref({
  leaveTypeId: "",
  startDate: "",
  endDate: "",
  totalShift: 1,
  reason: "",
});

onMounted(async () => {
  const res = await leaveTypeService.getAll();
  leaveTypes.value = res.data;
});

const submitLeave = async () => {
  try {
    await leaveService.createLeave(form.value);
    alert("Gửi đơn thành công!");
  } catch (err) {
    console.error(err);
    alert("Gửi đơn thất bại");
  }
};
</script>

<template>
  <div class="card p-4">
    <h4>Đơn xin nghỉ phép</h4>

    <!-- Loại nghỉ -->
    <div class="mb-2">
      <label>Loại nghỉ</label>
      <select v-model="form.leaveTypeId" class="form-control">
        <option value="">-- chọn --</option>
        <option
            v-for="type in leaveTypes"
            :key="type.leaveTypeId"
            :value="type.leaveTypeId"
        >
          {{ type.typeName }}
        </option>
      </select>
    </div>

    <!-- Ngày bắt đầu -->
    <div class="mb-2">
      <label>Ngày bắt đầu</label>
      <input type="date" v-model="form.startDate" class="form-control" />
    </div>

    <!-- Ngày kết thúc -->
    <div class="mb-2">
      <label>Ngày kết thúc</label>
      <input type="date" v-model="form.endDate" class="form-control" />
    </div>

    <!-- Tổng số ca -->
    <div class="mb-2">
      <label>Tổng số ca</label>
      <input type="number" v-model="form.totalShift" class="form-control" />
    </div>

    <!-- Lý do -->
    <div class="mb-2">
      <label>Lý do</label>
      <textarea v-model="form.reason" class="form-control"></textarea>
    </div>

    <button @click="submitLeave" class="btn btn-primary">
      Gửi đơn
    </button>
  </div>
</template>