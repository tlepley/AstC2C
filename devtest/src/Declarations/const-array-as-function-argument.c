// The array is const, not 

int tab[3];

int f(const int p[3]) {
  p=tab;
}
