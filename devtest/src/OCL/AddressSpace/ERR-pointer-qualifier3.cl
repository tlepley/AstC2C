// pointer to __local
__local int *__constant p;

void k() {
  // Pointer to private
  int *q;

  // Error
  q=p;
}
