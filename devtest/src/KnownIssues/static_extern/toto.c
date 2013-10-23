// Result with gcc in both printf is '1'


extern int a;

void main() {
  printf("%d",a);
  f();
  h();
}


static int a = 1 ;

void f() {
  printf("   -> %d",a);

  if (1) {
    extern int a;
    printf("   -> %d",a);  
  }
}
