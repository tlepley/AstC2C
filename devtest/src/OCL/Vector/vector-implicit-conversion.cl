// Implicit conversion are allowed with scalar if there is no down-conversion
// No implicit conversion between vectors are allowed

int f() {
  char4 c4, c4_2;
  uchar4 uc4, uc4_2;
  short4 s4, s4_2;
  ushort4 us4, us4_2;
  int4 i4, i4_2;
  uint4 ui4, ui4_2;
  float4 f4, f4_2;

  c4='a';
  c4=c4_2;

  uc4='a'; // up-conversion (promotion)
  uc4=uc4_2;

  c4='a';
  c4=c4_2;

  uc4='a'; // up-conversion (promotion)
  uc4=uc4_2;

  i4='b';  // up-conversion
  i4=1;
  i4=i4_2;

  ui4='c';  // up-conversion
  ui4=1;    // up-conversion
  ui4=0xFFu;
  ui4=ui4_2;

  f4=f4_2;
}
