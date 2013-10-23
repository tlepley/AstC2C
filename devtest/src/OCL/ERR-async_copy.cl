// The OpenCL async copy function is overloaded. The call must be non ambiguous
int f() {
  event_t e;
  int length;
  __local int *pf1;
  __global unsigned int *pf2;

  async_work_group_copy(pf1,pf2,length,e);
}
