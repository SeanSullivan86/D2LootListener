package org.sully.d2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.tyrus.server.Server;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.gamemodel.enums.ItemQuality;
import org.sully.d2.gamemodel.staticgamedata.strings.D2String;
import org.sully.d2.gamemodel.staticgamedata.*;
import org.sully.d2.itemtracking.*;
import org.sully.d2.websocketserver.ClientRequestHandlerImpl;
import org.sully.d2.websocketserver.LootEndpoint;
import org.sully.d2.websocketserver.PeriodicItemBroadcaster;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class D2LootListener {

	public static void main(String[] args) {
		Server server = new Server("localhost", 8025, "/ws", null, LootEndpoint.class);
		try {
			run(server);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			server.stop();
		}
	}

	private static volatile ClientRequestHandlerImpl requestHandler;
	public synchronized static ClientRequestHandlerImpl getRequestHandler() {
		return requestHandler;
	}


	private static void run(Server server) throws Exception {
		int d2InstanceCount = 1;
		(new File("output")).mkdir();

		loadAndLinkStaticGameData();
		
		List<ItemConsumer> consumers = new ArrayList<>();

		ItemGrid countsByTypeAndQuality = new ItemGrid<String, String>(
				"Item Counts by Item Type and Quality",
				item -> item.getItemType().getName(),
				item -> item.getQuality().name(),
				item -> true,
				itemName -> itemName,
				quality -> quality,
				Comparator.naturalOrder(),
				Comparator.naturalOrder(),
				new File("output/itemTypeAndQualityGrid.csv"));

		ItemGrid uniquesAndSets = new ItemGrid<String, String>(
				"Counts of Set and Unique Items by Name",
				D2Item::getName,
				item -> item.isEthereal() ? "Ethereal" : "Non-Ethereal",
				item -> (item.getQuality() == ItemQuality.SET || item.getQuality() == ItemQuality.UNIQUE),
				name -> name,
				eth -> eth,
				Comparator.naturalOrder(),
				Comparator.naturalOrder(),
				new File("output/uniquesAndSets.csv"));


		ItemUseCaseTracker useCaseTracker = ItemUseCasesHardcodedConfiguration.createTrackerWithHardcodedItemUseCases();


		BasicStatsConsumer basicStatsConsumer = new BasicStatsConsumer();


		consumers.add(countsByTypeAndQuality);
		consumers.add(uniquesAndSets);
		// consumers.add(new PerfectUniquesTracker());
		consumers.add(useCaseTracker);
		consumers.add(basicStatsConsumer);

		//consumers.add(new NotifyPeriodicallyForRandomItems());
		ItemNotifier stdoutNotifier = new StdoutItemNotifier();

		Map<String,ItemGrid<?,?>> itemGrids = new ConcurrentHashMap<>();
        itemGrids.put(uniquesAndSets.getName(), uniquesAndSets);
        itemGrids.put(countsByTypeAndQuality.getName(), countsByTypeAndQuality);

        requestHandler = new ClientRequestHandlerImpl(useCaseTracker, basicStatsConsumer, itemGrids, new ObjectMapper());

		server.start();

		//PeriodicItemBroadcaster broadcaster = new PeriodicItemBroadcaster(countsByQuality);
		//Thread broadcasterThread = new Thread(broadcaster);
		//broadcasterThread.setDaemon(true);
		//broadcasterThread.start();

		byte[] itemBuffer = new byte[65536];

		D2DropContext[] dropContexts = new D2DropContext[d2InstanceCount];

		InputStream[] inputStreams = new InputStream[d2InstanceCount];
		for (int i = 0; i < d2InstanceCount; i++) {
			ServerSocket serverSocket = new ServerSocket(5430 + i);
			System.out.println("Waiting for connection on port " + (5430 + i));
			Socket clientSocket = serverSocket.accept();
			// PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			inputStreams[i] = new BufferedInputStream(clientSocket.getInputStream());
			System.out.println("Connection " + i + " established...");

			readFully(inputStreams[i], itemBuffer, 0, 28);
			ByteBuffer buf = ByteBuffer.wrap(itemBuffer);
			buf.order(ByteOrder.LITTLE_ENDIAN);

			dropContexts[i] = D2DropContext.builder()
					.treasureClassId(buf.getInt(8))
					.magicFind(buf.getInt(12))
					.unitTypeId(buf.getInt(16))
					.unitClassId(buf.getInt(20))
					.gameDifficulty(buf.getInt(24))
					.build();
		}
		//InputStream in = new BufferedInputStream(new FileInputStream("C:\\Users\\12063\\streamdata.bin"));
		InputStream in;

		try {
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

				readFully(in, itemBuffer, 0, multidropMessageSize - 16);
				buf = ByteBuffer.wrap(itemBuffer).order(ByteOrder.LITTLE_ENDIAN);

				tcDrop = D2TCDrop.fromData(itemBuffer, buf, dropContexts[d2InstanceIndex], multidropIterationInSingleGame, itemCountInMultidrop);

				//try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }


				iteration++;

				/*
				if (item.getItem().getQuality() == ItemQuality.RARE) {
					ObjectMapper jackson = new ObjectMapper();
					jackson.enable(SerializationFeature.INDENT_OUTPUT);
					System.out.println(jackson.writeValueAsString(item.getItem()));
					System.out.println(jackson.writeValueAsString(item.getItem()));
				} */

				/*
				try {
					for (ItemConsumer consumer : consumers) {
						consumer.consume(itemDrop, stdoutNotifier);
					}
				} catch (RuntimeException e) {
					System.out.println("Failed on item : " + itemDrop.getItem().toLongString());
					throw e;
				}

				if (iteration % 100000 == 0) {
					long newTimestamp = System.nanoTime();
					System.out.println("" + ((newTimestamp - lastTimestamp)/100_000.0));
					lastTimestamp = newTimestamp;
				}
				*/

/*
				if (i % 100 == 0) {
					try { Thread.sleep(1000); } catch (InterruptedException e) { throw new RuntimeException(e); }
				}

				
				if (i % 10_000_000 == 0) {
					useCaseTracker.closeAndGenerateOutput();
					System.out.println("Printed stats after " + i + " items.");
				} */
			}
		} catch (Throwable t) {
			System.out.println(t.getMessage());
			t.printStackTrace();
		} finally {
			for (ItemConsumer consumer : consumers) {
				consumer.closeAndGenerateOutput();
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
            int count = in.read(b, off + n, len - n);
            if (count < 0) {
				System.out.println("End of Stream 2222");
				try { Thread.sleep(1_000_000_000); } catch (InterruptedException e) { throw new RuntimeException(e); }
                throw new RuntimeException("End of Stream...");
            }
            n += count;
        }
    }
}