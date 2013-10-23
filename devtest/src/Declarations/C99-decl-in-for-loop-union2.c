// Union declaration forbidden in for-loop
int f() {
  for(union {int j;} i;0;0) 0;
}
