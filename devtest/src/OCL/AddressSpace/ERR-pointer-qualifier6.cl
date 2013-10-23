// pointer to __constant
__constant *__constant p;

void k() {
  // Pointer to private
  __global int *q;

  // Error
  q=p;
}

