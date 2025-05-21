class UIDGenerator {
  static int _s_id = 0;

  static int get next => _s_id++;
}
