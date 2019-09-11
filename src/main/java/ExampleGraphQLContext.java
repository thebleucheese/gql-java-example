import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

public class ExampleGraphQLContext {
	DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();

	public ExampleGraphQLContext() {
		// @NOTE!! If you comment out the following line, the execution of this program will hang
		dataLoaderRegistry.register("Account", ExampleGraphQLWiring.buildNewExampleDataLoader(this));
	}

	public DataLoaderRegistry getDataLoaderRegistry() {
		return dataLoaderRegistry;
	}

	public DataLoader<String, Object> getExampleDataLoader(String type) {
		if(dataLoaderRegistry.getKeys().contains(type)) {
			return dataLoaderRegistry.getDataLoader(type);
		}

		DataLoader dl = ExampleGraphQLWiring.buildNewExampleDataLoader(this);

		dataLoaderRegistry.register(type, dl);
		return dl;
	}
}
