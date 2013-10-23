// pointer to __constant
__global *__constant p;

void k() {
  // Pointer to private
  __constant int *q;

  // Error since the address space is kept with +4
  q=p+4;
}

