// pointer to __constant
__global *__constant p;

void k() {
  // Pointer to private
  __constant int *q;

  // Error
  q=p;
}

