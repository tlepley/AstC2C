// The 'void' type is authorized in the conditional is both are 'void'

void g();

int f(int i) {
  i ? (void)0: g();
}
