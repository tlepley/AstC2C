// Extern variable declaration forbidden in for-loop
int f() {
  for(extern int i;i<10;i++) i=i+1;
}
