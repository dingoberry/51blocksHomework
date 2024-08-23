import 'package:flutter/cupertino.dart';

import 'detail.dart';

Route navigator2detail(String id) {
  return PageRouteBuilder(
      pageBuilder: (context, animation, secondaryAnimation) =>
          MyDetailApp(id: id),
      transitionsBuilder: (context, animation, secondaryAnimation, child) {
        return SlideTransition(
            position: Tween<Offset>(
              begin: const Offset(1.0, 0.0),
              end: Offset.zero, // Slide to the right
            ).animate(animation),
            child: child);
      });
}
