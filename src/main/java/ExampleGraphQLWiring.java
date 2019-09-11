import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import org.dataloader.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ExampleGraphQLWiring {

	private static BatchLoaderWithContext<String, Map<String,String>> exampleDataBatchLoader = new BatchLoaderWithContext<String, Map<String, String>>() {

		@Override
		public CompletionStage<List<Map<String, String>>> load(List<String> keys, BatchLoaderEnvironment loaderContext) {

			ExampleGraphQLContext ctx = loaderContext.getContext();

			return CompletableFuture.supplyAsync(() -> {
				List<Map<String,String>> loadedData = new ArrayList<>();

				// Mocking the data loading here so I don't need to include a DB
				loadedData.add(new HashMap<String, String>() {
					{
						put("id", "860cc9bc-4da2-4698-889b-e96cca9e4bf3");
						put("name", "test layer");
					}});

				return loadedData;
			});
		}
	};

	public static DataLoader<String, Map<String, String>> buildNewExampleDataLoader(ExampleGraphQLContext queryContext) {
		BatchLoaderContextProvider contextProvider = new BatchLoaderContextProvider() {
			@Override
			public Object getContext() {
				return queryContext;
			}
		};
		DataLoaderOptions loaderOptions = DataLoaderOptions.newOptions().setBatchLoaderContextProvider(contextProvider);
		return DataLoader.newDataLoader(exampleDataBatchLoader, loaderOptions);
	}

	static DataFetcher exampleDataFetcher = new DataFetcher() {
		@Override
		public Object get(DataFetchingEnvironment environment) {
			List<String> argIds = environment.getArgument("ids");
			ExampleGraphQLContext ctx = environment.getContext();

			// get type from environment data
			GraphQLFieldDefinition envFieldDef = environment.getFieldDefinition();
			GraphQLOutputType type = envFieldDef.getType();

			String fieldTypeName;

			GraphQLType fieldType = envFieldDef.getType();
			if (fieldType instanceof GraphQLList) {
				fieldType = ((GraphQLList) type).getWrappedType();
				fieldTypeName = fieldType.getName();
			} else {
				fieldTypeName = fieldType.getName();
			}

			return ctx.getExampleDataLoader(fieldTypeName).loadMany(argIds);
		}
	};


	public static RuntimeWiring buildRuntimeWiring() {
		return RuntimeWiring.newRuntimeWiring()
				.type("Query", typeWiring -> typeWiring
						.dataFetcher("hello", new StaticDataFetcher("world"))
						.dataFetcher("accounts", exampleDataFetcher)
						.dataFetcher("layers", exampleDataFetcher)
				)
				.build();
	}
}
