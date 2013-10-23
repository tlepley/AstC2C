// pointer to __constant
__constant *__constant p;

void k() {
  // Pointer to private
  __local int *q;

  // Error
  q=p;
}

