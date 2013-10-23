// pointer to __global
__global int *__constant p;

void k() {
  // Pointer to private
  int *q;

  // Error
  q=p;
}

