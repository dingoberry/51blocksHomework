import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:graphql_flutter/graphql_flutter.dart';

import 'hepls.dart';

String _readRepositories = """
query Person(\$personId: ID) {
    person(id: \$personId) {
        mass
        id
        name
        homeworld {
            climates
            created
            diameter
            edited
            gravity
            id
            name
            orbitalPeriod
            population
            rotationPeriod
            surfaceWater
            terrains
        }
        height
    }
}
""";

class MyDetailApp extends StatelessWidget {
  const MyDetailApp({super.key, required this.id});

  final String id;

  @override
  Widget build(BuildContext context) {
    return GraphQLProvider(
      client: c,
      child: MaterialApp(
        title: 'People',
        theme: ThemeData(
            colorScheme: null,
            useMaterial3: true,
            textTheme: const TextTheme(
                bodyMedium: TextStyle(color: Colors.white),
                bodyLarge: TextStyle(color: Colors.white),
                bodySmall: TextStyle(color: Colors.white))),
        home: CacheProvider(child: MyDetailHomePage(title: 'People', id: id)),
      ),
    );
  }
}

class MyDetailHomePage extends StatefulWidget {
  const MyDetailHomePage({super.key, required this.title, required this.id});

  final String title;

  final String id;

  @override
  State<MyDetailHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyDetailHomePage>
    with TickerProviderStateMixin {
  bool _showPopDetail = false;

  late AnimationController _controller;
  late Animation<Offset> _animation;

  @override
  void dispose() {
    // 确保AnimationController的dispose被调用
    _controller.dispose();
    super.dispose();
  }

  @override
  void initState() {
    super.initState();

    _controller = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 2500));
    _animation = Tween<Offset>(
            begin: const Offset(0, 1), end: const Offset(0, 0))
        .animate(
            CurvedAnimation(parent: _controller, curve: Curves.fastOutSlowIn));
    _controller.forward();
  }

  void _showPopMyDetail() {
    setState(() {
      _showPopDetail = true;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        width: MediaQuery.of(context).size.width,
        decoration: const BoxDecoration(
            gradient: LinearGradient(
                colors: [Color(0xFF1E1266), Colors.black],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter)),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(left: 10, top: 30, bottom: 20),
              child: Row(
                children: [
                  const Icon(Icons.arrow_back_ios_outlined,
                      color: Colors.white),
                  Text(widget.title, style: const TextStyle(fontSize: 20.0))
                ],
              ),
            ),
            Query(
                options: QueryOptions(
                    document: gql(_readRepositories),
                    variables: {'personId': widget.id}),
                builder: withGenericHandling((QueryResult result,
                    {refetch, fetchMore}) {
                  if (result.data == null && !result.hasException) {
                    return const Text(
                      'Loading has completed, but both data and errors are null. '
                      'This should never be the case – please open an issue',
                    );
                  }

                  final person =
                      (result.data!['person'] as Map<String, dynamic>);

                  return Expanded(
                      child: Stack(children: [
                    Padding(
                        padding: const EdgeInsets.all(20),
                        child: RichText(
                            text: TextSpan(children: [
                              const TextSpan(
                                  text: "Click ", style: TextStyle()),
                              TextSpan(
                                  recognizer: TapGestureRecognizer()
                                    ..onTap = () => _showPopMyDetail(),
                                  text: "here",
                                  style: const TextStyle(
                                      color: Colors.blue,
                                      decoration: TextDecoration.underline)),
                              TextSpan(
                                  text:
                                      " to view homeworld data for ${person['name']}",
                                  style: const TextStyle())
                            ]),
                            textScaler: const TextScaler.linear(1.35))),
                    if (_showPopDetail)
                      Positioned(
                          left: 0.0,
                          right: 0.0,
                          bottom: 0.0,
                          child: SlideTransition(
                              position: _animation,
                              child: Container(
                                decoration: const BoxDecoration(
                                    color: Colors.white,
                                    borderRadius: BorderRadius.only(
                                        topLeft: Radius.circular(15),
                                        topRight: Radius.circular(15))),
                                child: Column(
                                  children: [
                                    Text("${person['name']}",
                                        style: const TextStyle(
                                            color: Colors.black, fontSize: 25)),
                                    Padding(
                                        padding: EdgeInsets.all(6),
                                        child: Text(
                                            style: const TextStyle(
                                                color: Colors.black),
                                            """homeworld : ${person['homeworld']['name']}
id : ${person['homeworld']['id']}
created : ${person['homeworld']['created']}
edited : ${person['homeworld']['edited']}
gravity : ${person['homeworld']['gravity']}
diameter : ${person['homeworld']['diameter']}
orbitalPeriod : ${person['homeworld']['orbitalPeriod']}
population : ${person['homeworld']['population']}
rotationPeriod : ${person['homeworld']['rotationPeriod']}
surfaceWater : ${person['homeworld']['surfaceWater']}
climates : ${person['homeworld']['climates']}
terrains : ${person['homeworld']['terrains']}
                                    """))
                                  ],
                                ),
                              )))
                  ]));
                }))
          ],
        ),
      ),
      // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
