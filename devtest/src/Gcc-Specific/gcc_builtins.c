//
// Some gcc builtins
//

int main() {
  int i;
  long l;
  float f;

  __builtin_fabs(i);
  __builtin_fabsf(f);
  __builtin_fabsl(l);
  __builtin_constant_p(i);

  return(1);
}
