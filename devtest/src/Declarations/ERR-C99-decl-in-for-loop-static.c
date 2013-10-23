// Static variable declaration forbidden in for-loop
int f() {
  for(static int i;i<10;i++) i=i+1;
}
