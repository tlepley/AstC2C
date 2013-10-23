// pointer to __constant
__constant *__constant p;

void k() {
  // Pointer to private
  int *q;

  // Error
  q=p;
}

