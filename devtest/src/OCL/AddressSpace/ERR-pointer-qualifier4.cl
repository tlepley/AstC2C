// pointer to __constant
__global *__constant p;

void k() {
  // Pointer to private
  __private int *q;

  // Error
  q=p;
}
