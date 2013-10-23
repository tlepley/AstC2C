// Enum declaration forbidden in for-loop
int f() {
  for(enum E {A} i;0;0) 0;
}
