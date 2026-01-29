package org.sully.d2;

import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.staticgamedata.strings.D2String;
import org.sully.d2.gamemodel.staticgamedata.*;
import org.sully.d2.itemtracking.*;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class D2LootListener {

	public static void main(String[] args) throws Exception {
		run();
	}




	private static void run() throws Exception {
		int d2InstanceCount = 1;
		String snapshotFolder = "C:\\Users\\sully\\D2LootSnapshots";

		SnapshotManager snapshotManager = new SnapshotManager(snapshotFolder);

		loadAndLinkStaticGameData();

		DropContextEnum[] dropContextsByGameIndex = new DropContextEnum[d2InstanceCount];

		byte[] itemBuffer = new byte[65536];
		InputStream[] inputStreams = new InputStream[d2InstanceCount];
		for (int i = 0; i < d2InstanceCount; i++) {

			Socket clientSocket = new Socket((String) null, 5430 + i);

			inputStreams[i] = new BufferedInputStream(clientSocket.getInputStream());
			System.out.println("Connection " + i + " established...");

			readFully(inputStreams[i], itemBuffer, 0, 28);
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

		DataSnapshot dataSnapshot = snapshotManager.retrieveMostRecentSnapshot();

		Set<DropContextEnum> dropContextsFromD2Instances = new HashSet<>();
		dropContextsFromD2Instances.addAll(Arrays.asList(dropContextsByGameIndex));

		Set<DropContextEnum> dropContextsFromSnapshot = dataSnapshot == null ? Set.of() : dataSnapshot.getDropContexts().stream()
				.map(x -> DropContextEnum.valueOf(x.getDropContextName()))
				.collect(Collectors.toSet());

		Set<DropContextEnum> allDropContexts = new HashSet<>(dropContextsFromSnapshot);
        allDropContexts.addAll(dropContextsFromD2Instances);


		HardcodedTCDropConsumerConfiguration consumerConfig = new HardcodedTCDropConsumerConfiguration();

		Map<DropContextEnum,List<D2TCDropConsumer>> consumersByDropContext = consumerConfig.initializeConsumers(dropContextsFromD2Instances);

		DataSnapshot previousSnapshot = dataSnapshot;

		//InputStream in = new BufferedInputStream(new FileInputStream("C:\\Users\\12063\\streamdata.bin"));
		InputStream in;

		long nextSnapshotTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);


		long iteration = 0;
		long lastTimestamp = 0;
		ByteBuffer buf;
		int multidropMessageSize;
		long multidropIterationInSingleGame;
		int itemCountInMultidrop;
		int d2InstanceIndex;

		D2TCDrop tcDrop;

		while (true) {
			d2InstanceIndex = (int) (iteration % d2InstanceCount);
			in = inputStreams[d2InstanceIndex];


			readFully(in, itemBuffer, 0, 16);
			buf = ByteBuffer.wrap(itemBuffer).order(ByteOrder.LITTLE_ENDIAN);
			multidropMessageSize = buf.getInt(0);
			multidropIterationInSingleGame = buf.getLong(4);
			itemCountInMultidrop = buf.getInt(12);

			/*System.out.println("Multidrop iteration " + multidropIterationInSingleGame + ", messageSize " + multidropMessageSize
					+ " itemCount " + itemCountInMultidrop);*/

			readFully(in, itemBuffer, 0, multidropMessageSize - 16);
			buf = ByteBuffer.wrap(itemBuffer).order(ByteOrder.LITTLE_ENDIAN);

			tcDrop = D2TCDrop.fromData(itemBuffer, buf, dropContextsByGameIndex[d2InstanceIndex], multidropIterationInSingleGame, itemCountInMultidrop);

			//try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }



			for (D2TCDropConsumer consumer : consumersByDropContext.get(dropContextsByGameIndex[d2InstanceIndex])) {
				consumer.consume(tcDrop);
			}
			// todo also consume again for special "ALL" dropContext ?


			iteration++;

			if (iteration % 1000 == 0 && System.nanoTime() > nextSnapshotTime) {
				nextSnapshotTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);

				List<SingleDropContextSnapshot> dropContextSnapshots = new ArrayList<>();
				Map<Long,SerializableD2Item> itemsReferencedInSnapshots = new HashMap<>();

				for (DropContextEnum dropContext : allDropContexts) {
					Map<String,TCDropConsumerSnapshot> consumerSnapshotsByName = new HashMap<>();
					if (consumersByDropContext.containsKey(dropContext)) {
						for (D2TCDropConsumer consumer : consumersByDropContext.get(dropContext)) {
							DataReferencingItems<TCDropConsumerSnapshot> consumerSnapshot = consumer.takeSnapshot();
							consumerSnapshot.getItems().forEach(item -> itemsReferencedInSnapshots.put(item.getId(), item.toSerializableD2Item()));
							consumerSnapshotsByName.put(consumerSnapshot.getData().getName(), consumerSnapshot.getData());
						}
					}
					if (previousSnapshot != null) {
						Optional<SingleDropContextSnapshot> previousSnapshotForThisDropContext = previousSnapshot.getDropContexts().stream()
								.filter(x -> x.getDropContextName().equals(dropContext.name())).findFirst();
						if (previousSnapshotForThisDropContext.isPresent()) {
							for (TCDropConsumerSnapshot consumerSnapshot : previousSnapshotForThisDropContext.get().getConsumers()) {
								if (!consumerSnapshotsByName.containsKey(consumerSnapshot.getName())) {
									consumerSnapshotsByName.put(consumerSnapshot.getName(), consumerSnapshot);
									for (Long referencedItemId : consumerSnapshot.getReferencedItemIds()) {
										itemsReferencedInSnapshots.put(referencedItemId, previousSnapshot.getItemsById().get(referencedItemId));
									}
								}
							}
						}
					}




					dropContextSnapshots.add(SingleDropContextSnapshot.builder()
							.dropContextName(dropContext.name())
							.consumers(List.copyOf(consumerSnapshotsByName.values()))
							.build());
				}

				DataSnapshot fullSnapshot = DataSnapshot.builder()
						.dropContexts(dropContextSnapshots)
						.itemsById(itemsReferencedInSnapshots)
						.nextItemId(D2Item.nextId)
						.build();

				System.out.println("Saving snapshot...");
				snapshotManager.saveSnapshot(fullSnapshot);
				System.out.println("Finished saving snapshot...");

				// todo save the snapshot somewhere ?
				// todo send the snapshot to a different server ?

				previousSnapshot = fullSnapshot;
			}
		}



	}

	private static void loadAndLinkStaticGameData() {
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
	
    static void readFully(InputStream in, byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
			int count = 0;
            try {
				count = in.read(b, off + n, len - n);
			} catch (Exception e) {
				throw new RuntimeException("Failed when reading input stream...", e);
			}

            if (count < 0) {
				System.out.println("End of Stream 2222");
				try { Thread.sleep(1_000_000_000); } catch (InterruptedException e) { throw new RuntimeException(e); }
                throw new RuntimeException("End of Stream...");
            }
            n += count;
        }
    }
}