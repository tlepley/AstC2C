// Struct declaration forbidden in for-loop
int f() {
  for(struct S {int j;} i;0;0) 0;
}
