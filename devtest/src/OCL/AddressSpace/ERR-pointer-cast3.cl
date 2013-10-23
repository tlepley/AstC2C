// Casting a pointer to an other address speace is forbidden

void f() {
  __private int * p;

  (__constant int *)p;
}

