// Vector literals

int f() {
  // Initialization
  int2 i2=(int2)(1);
  int2 i2_1=(int2)(1,2);
  int4 i4=(int4)(1);
  int4 i4_1=(int4)(1,2,3,4);
  int4 i4_2=(int4)(1,2,(int2)(10,20));

  // Asignment
  i2=(int2)(2);
  i2=(int2)(2,3);
  i4=(int4)(2);
  i4=(int4)(2,3,4,5);

  // Expression
  i2=i2+(int2)(4);
  i2=i2+(int2)(4,5);
  i4=i4+(int4)(4);
  i4=i4+(int4)(4,5,6,7);
}
