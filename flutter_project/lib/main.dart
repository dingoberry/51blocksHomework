import 'package:flutter/material.dart';
import 'package:flutter_project/router.dart';
import 'package:graphql_flutter/graphql_flutter.dart';

import 'hepls.dart';

String readRepositories = """
query People {
    allPeople {
        people {
            name
            id
            mass
            height
        }
    }
}
""";

void main() async {
  await initRequestClient();
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

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
        home: const CacheProvider(child: MyHomePage(title: 'People')),
      ),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
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
          // mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(left: 30, top: 30, bottom: 20),
              child: Text(widget.title, style: const TextStyle(fontSize: 38.0)),
            ),
            const Divider(height: 1, color: Colors.grey),
            Query(
                options: QueryOptions(document: gql(readRepositories)),
                builder: withGenericHandling((QueryResult result,
                    {refetch, fetchMore}) {
                  if (result.data == null && !result.hasException) {
                    return const Text(
                      'Loading has completed, but both data and errors are null. '
                      'This should never be the case – please open an issue',
                    );
                  }

                  final repositories =
                      (result.data!['allPeople']['people'] as List<dynamic>);

                  return Expanded(
                    child: ListView.builder(
                      padding: EdgeInsets.zero,
                      itemCount: repositories.length,
                      itemBuilder: (BuildContext context, int index) {
                        var item = repositories[index];
                        return InkWell(
                            child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Padding(
                                      padding: const EdgeInsets.only(
                                          top: 15, bottom: 15, left: 18, right: 18),
                                      child: Row(
                                          crossAxisAlignment:
                                              CrossAxisAlignment.start,
                                          children: [
                                            Expanded(
                                                child: Column(
                                                    crossAxisAlignment:
                                                        CrossAxisAlignment
                                                            .start,
                                                    children: [
                                                  Text(item["name"]),
                                                  Text(
                                                      "Height: ${item["mass"]?.toString() ?? ""}"),
                                                  Text(
                                                      "Mass: ${item["height"]?.toString() ?? ""}"),
                                                ])),
                                            const Icon(
                                              Icons.arrow_forward_ios_sharp,
                                              color: Colors.white,
                                            )
                                          ])),
                                  const Divider(height: 1, color: Colors.white),
                                ]),
                            onTap: () {
                              Navigator.of(context)
                                  .push(navigator2detail(item["id"]));
                            });
                      },
                    ),
                  );
                }))
          ],
        ),
      ),
      // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
