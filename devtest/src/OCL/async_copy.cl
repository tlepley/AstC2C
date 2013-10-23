// The OpenCL async copy function is overloaded. It allows copying from __local to __global and from __global to __local memory

int f() {
  event_t e;
  int length;
  __local float *pf1;
  __global float *pf2;
  __local int *pi1;
  __global int *pi2;
  __local unsigned int *pui1;
  __global unsigned int *pui2;

  async_work_group_copy(pf1,pf2,length,e);
  async_work_group_copy(pf2,pf1,length,e);
  async_work_group_copy(pi1,pi2,length,e);
  async_work_group_copy(pi2,pi1,length,e);
  async_work_group_copy(pui1,pui2,length,e);
  async_work_group_copy(pui2,pui1,length,e);
}
