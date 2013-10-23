// Struct declaration forbidden in for-loop
int f() {
  for(struct {int j;} i;0;0) 0;
}
