// Casting a pointer to an other address speace is forbidden

void f() {
  __local int * p;

  (__constant int *)p;
}

