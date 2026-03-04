package org.sully.d2;

import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.staticgamedata.strings.D2String;
import org.sully.d2.gamemodel.staticgamedata.*;
import org.sully.d2.itemtracking.*;
import org.sully.d2.util.IOUtils;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class D2LootListener {

	public static void main(String[] args) throws Exception {
		run();
	}

	private static void run() throws Exception {
		int d2InstanceCount = 1;
		String snapshotFolder = "C:\\Users\\sully\\D2LootSnapshots";

		SnapshotManager snapshotManager = new SnapshotManager(snapshotFolder);

		loadAndLinkStaticGameData();

		HardcodedTCDropConsumerConfiguration consumerConfig = new HardcodedTCDropConsumerConfiguration();



		DropContextEnum[] dropContextsByGameIndex = new DropContextEnum[d2InstanceCount];

		byte[] itemBuffer = new byte[65536];
		InputStream[] inputStreams = new InputStream[d2InstanceCount];
		for (int i = 0; i < d2InstanceCount; i++) {

			Socket clientSocket = new Socket((String) null, 5430 + i);

			inputStreams[i] = new BufferedInputStream(clientSocket.getInputStream());
			System.out.println("Connection " + i + " established...");

			IOUtils.readFully(inputStreams[i], itemBuffer, 0, 28);
			ByteBuffer buf = ByteBuffer.wrap(itemBuffer);
			buf.order(ByteOrder.LITTLE_ENDIAN);

			D2DropContext dropContext = D2DropContext.builder()
					.treasureClassId(buf.getInt(8))
					.magicFind(buf.getInt(12))
					.unitTypeId(buf.getInt(16))
					.unitClassId(buf.getInt(20))
					.gameDifficulty(buf.getInt(24))
					.build();
			dropContextsByGameIndex[i] = DropContextEnum.getFromDropContextDetails(dropContext);

		}


		Set<DropContextEnum> dropContextsFromD2Instances = new HashSet<>(Arrays.asList(dropContextsByGameIndex));
		Set<DropContextEnum> allDropContexts = new HashSet<>(dropContextsFromD2Instances);


		Map<DropContextEnum,List<D2TCDropConsumer>> consumersByDropContext = consumerConfig.initializeConsumers(dropContextsFromD2Instances);

		DataSnapshot previousSnapshot = null;

		Optional<DataSnapshot> dataSnapshotFromStorage = snapshotManager.retrieveMostRecentSnapshot();
		if (dataSnapshotFromStorage.isPresent()) {
			System.out.println("Loading Data Snapshot from storage ...");
			dataSnapshotFromStorage.get().logSummary(System.out::println);

			D2Item.nextId = dataSnapshotFromStorage.get().getNextItemId();
			dataSnapshotFromStorage.get().addSnapshotDataToConsumers(consumersByDropContext);

			for (SingleDropContextSnapshot dropContextFromSnapshot : dataSnapshotFromStorage.get().getDropContexts().values()) {
				allDropContexts.add(DropContextEnum.valueOf(dropContextFromSnapshot.getDropContextName()));
			}
			previousSnapshot = dataSnapshotFromStorage.get();
		} else {
			System.out.println("Warning : No previous Data Snapshot found");
		}

		//InputStream in = new BufferedInputStream(new FileInputStream("C:\\Users\\12063\\streamdata.bin"));
		InputStream in;

		long nextSnapshotTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(300);


		long iteration = 0;
		ByteBuffer buf;
		int multidropMessageSize;
		int itemCountInMultidrop;
		int d2InstanceIndex;

		D2TCDrop tcDrop;

		long lastMillionItemTimestamp = System.currentTimeMillis();

		while (true) {
			d2InstanceIndex = (int) (iteration % d2InstanceCount);
			in = inputStreams[d2InstanceIndex];


			IOUtils.readFully(in, itemBuffer, 0, 16);
			buf = ByteBuffer.wrap(itemBuffer).order(ByteOrder.LITTLE_ENDIAN);
			multidropMessageSize = buf.getInt(0);
			itemCountInMultidrop = buf.getInt(12);

			/*System.out.println("Multidrop iteration " + multidropIterationInSingleGame + ", messageSize " + multidropMessageSize
					+ " itemCount " + itemCountInMultidrop);*/

			IOUtils.readFully(in, itemBuffer, 0, multidropMessageSize - 16);
			buf = ByteBuffer.wrap(itemBuffer).order(ByteOrder.LITTLE_ENDIAN);

			tcDrop = D2TCDrop.fromData(itemBuffer, buf, dropContextsByGameIndex[d2InstanceIndex], itemCountInMultidrop);

			//try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }



			for (D2TCDropConsumer consumer : consumersByDropContext.get(dropContextsByGameIndex[d2InstanceIndex])) {
				consumer.consume(tcDrop);
			}

			iteration++;

			if (iteration % 100_000 == 0) {
				long newTimestamp = System.currentTimeMillis();
				System.out.println(iteration + " drops done. Last 100k in " + (newTimestamp - lastMillionItemTimestamp) + " ms. (" +
								String.format("%.3f", (100_000 * 1000.0 / (newTimestamp - lastMillionItemTimestamp))) + " per second)");
				lastMillionItemTimestamp = newTimestamp;
			}

			if (iteration % 1000 == 0 && System.nanoTime() > nextSnapshotTime) {
				long nanoTimeAtStartOfSnapshotting = System.nanoTime();


				Map<DropContextEnum,SingleDropContextSnapshot> dropContextSnapshots = new HashMap<>();
				Map<Long,SerializableD2Item> itemsReferencedInSnapshots = new HashMap<>();

				for (DropContextEnum dropContext : allDropContexts) {
					Map<String,TCDropConsumerSnapshot> consumerSnapshotsById = new HashMap<>();
					if (consumersByDropContext.containsKey(dropContext)) {
						for (D2TCDropConsumer consumer : consumersByDropContext.get(dropContext)) {
							DataReferencingItems<TCDropConsumerSnapshot> consumerSnapshot = consumer.takeSnapshot();
							consumerSnapshot.getItems().forEach(item -> itemsReferencedInSnapshots.put(item.getId(), item.toSerializableD2Item()));
							consumerSnapshotsById.put(consumerSnapshot.getData().getId(), consumerSnapshot.getData());
						}
					}
					if (previousSnapshot != null) {
						Optional<SingleDropContextSnapshot> previousSnapshotForThisDropContext = Optional.ofNullable(previousSnapshot.getDropContexts().get(dropContext));

						if (previousSnapshotForThisDropContext.isPresent()) {
							for (TCDropConsumerSnapshot consumerSnapshot : previousSnapshotForThisDropContext.get().getConsumersById().values()) {
								if (!consumerSnapshotsById.containsKey(consumerSnapshot.getId())) {
									consumerSnapshotsById.put(consumerSnapshot.getId(), consumerSnapshot);
									for (Long referencedItemId : consumerSnapshot.getReferencedItemIds()) {
										itemsReferencedInSnapshots.put(referencedItemId, previousSnapshot.getItemsById().get(referencedItemId));
									}
								}
							}
						}
					}

					dropContextSnapshots.put(dropContext,
							SingleDropContextSnapshot.builder()
									.dropContextName(dropContext.name())
									.consumersById(consumerSnapshotsById)
									.build());
				}

				// Initialize Empty Consumers for the "ALL" DropContext
				Map<String, D2TCDropConsumer> aggregateConsumersById = new HashMap<>();
				for (D2TCDropConsumer aggregateConsumer : consumerConfig.initializeConsumersForSingleDropContext(DropContextEnum.ALL)) {
					aggregateConsumersById.put(aggregateConsumer.getId(), aggregateConsumer);
				}

				// Increment the stats in the "ALL" consumers, from the snapshots we've already generated for the individual dropContexts
				for (SingleDropContextSnapshot singleDropContextSnapshot : dropContextSnapshots.values()) {
					for (TCDropConsumerSnapshot consumerSnapshot : singleDropContextSnapshot.getConsumersById().values()) {
						if (aggregateConsumersById.containsKey(consumerSnapshot.getId())) {
							aggregateConsumersById.get(consumerSnapshot.getId()).incrementFromSnapshot(consumerSnapshot, itemsReferencedInSnapshots);
						}
					}
				}

				// Generate the Snapshots of each of the consumers for the "ALL" DropContext
				Map<String, TCDropConsumerSnapshot> aggregateConsumerSnapshotsById = new HashMap<>();
				for (D2TCDropConsumer aggregateConsumer : aggregateConsumersById.values()) {
					aggregateConsumerSnapshotsById.put(aggregateConsumer.getId(), aggregateConsumer.takeSnapshot().getData());
				}

				DataSnapshot newSnapshot = DataSnapshot.builder()
						.dropContexts(dropContextSnapshots)
						.aggregatesOverAllDropContexts(SingleDropContextSnapshot.builder()
								.dropContextName(DropContextEnum.ALL.name())
								.consumersById(aggregateConsumerSnapshotsById)
								.build())
						.itemsById(itemsReferencedInSnapshots)
						.nextItemId(D2Item.nextId)
						.id(SnapshotManager.generateSnapshotId())
						.build();


				snapshotManager.saveSnapshot(newSnapshot);


				long nanoTimeAtEndOfSnapshotting = System.nanoTime();

				System.out.println("Finished saving snapshot. Time spent = " + String.format("%.1f", ((nanoTimeAtEndOfSnapshotting - nanoTimeAtStartOfSnapshotting) / 1_000_000.0)) + " ms");

				nextSnapshotTime = nanoTimeAtEndOfSnapshotting + TimeUnit.SECONDS.toNanos(300);

				// todo send the snapshot to a different server ?

				previousSnapshot = newSnapshot;
			}
		}



	}

	static void loadAndLinkStaticGameData() {
		D2ItemType.loadData();
		D2ItemTypeType.loadData();
		D2Skill.loadData();
		D2String.loadData();

		D2ItemStat.loadData();
		D2Property.loadData();
		D2UniqueItem.loadData();

		D2ItemType.linkData();
		D2Skill.linkData();

		D2Property.linkData();
		D2UniqueItem.linkData();
	}
	

}